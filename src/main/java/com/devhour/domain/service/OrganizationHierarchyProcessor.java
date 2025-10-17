package com.devhour.domain.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.devhour.domain.model.valueobject.EmployeeRecord;
import com.devhour.domain.model.valueobject.Position;

import lombok.extern.slf4j.Slf4j;

/**
 * 組織階層処理ドメインサービス
 * 
 * 組織階層に基づいて承認者関係を計算する純粋なドメインサービス。
 * データベースアクセスを行わず、メモリ上の社員情報から承認関係を判定する。
 * 
 * 承認ルール:
 * - 一般社員: 同グループのマネージャー → 同部の部長
 * - マネージャー: 同部の部長
 * - 部長: 同本部の本部長  
 * - 本部長: 同統括本部の統括本部長
 * - 統括本部長: 承認者なし
 * 
 * 特徴:
 * - ステートレス（状態を持たない）
 * - 純粋なビジネスロジック（外部依存なし）
 * - 高性能（メモリベース処理）
 * - 大規模データセット対応
 */
@Component
@Slf4j
public class OrganizationHierarchyProcessor {
    
    /**
     * メモリ上で承認者を計算（DBクエリなし）
     * 
     * @param allEmployees 全社員情報のマップ（メールアドレス -> EmployeeRecord）
     * @return 承認者関係のマップ（対象者メール -> 承認者メールのSet）
     */
    public Map<String, Set<String>> calculateApprovers(Map<String, EmployeeRecord> allEmployees) {
        if (allEmployees == null || allEmployees.isEmpty()) {
            log.debug("Empty employee map provided, returning empty approver relations");
            return new HashMap<>();
        }
        
        log.debug("Calculating approvers for {} employees", allEmployees.size());
        
        Map<String, Set<String>> approverRelations = new HashMap<>();
        
        // 組織階層マップを構築（コードベースでグルーピング）
        Map<String, List<EmployeeRecord>> byLevel1 = groupBy(allEmployees.values(), EmployeeRecord::level1Code);
        Map<String, List<EmployeeRecord>> byLevel2 = groupBy(allEmployees.values(), EmployeeRecord::level2Code);  
        Map<String, List<EmployeeRecord>> byLevel3 = groupBy(allEmployees.values(), EmployeeRecord::level3Code);
        Map<String, List<EmployeeRecord>> byLevel4 = groupBy(allEmployees.values(), EmployeeRecord::level4Code);
        
        log.debug("Organization hierarchy built - L1: {}, L2: {}, L3: {}, L4: {}", 
                 byLevel1.size(), byLevel2.size(), byLevel3.size(), byLevel4.size());
        
        for (EmployeeRecord employee : allEmployees.values()) {
            Set<String> approvers = new HashSet<>();
            
            try {
                // Position enumを使用して判定
                Position position = Position.fromJapaneseName(employee.position());
                
                switch (position) {
                    case EMPLOYEE:
                        // 一般社員の場合: 同じグループのマネージャーを探す
                        if (employee.level4Code() != null) {
                            findApproverByPosition(byLevel4.get(employee.level4Code()), Position.MANAGER.getJapaneseName())
                                .ifPresent(approvers::add);
                        }
                        // マネージャーがいない場合は部長
                        if (approvers.isEmpty() && employee.level3Code() != null) {
                            findApproverByPosition(byLevel3.get(employee.level3Code()), Position.DEPARTMENT_MANAGER.getJapaneseName())
                                .ifPresent(approvers::add);
                        }
                        break;
                        
                    case MANAGER:
                        // 同じ部の部長を探す
                        if (employee.level3Code() != null) {
                            findApproverByPosition(byLevel3.get(employee.level3Code()), Position.DEPARTMENT_MANAGER.getJapaneseName())
                                .ifPresent(approvers::add);
                        }
                        break;
                        
                    case DEPARTMENT_MANAGER:
                        // 同じ本部の本部長を探す
                        if (employee.level2Code() != null) {
                            findApproverByPosition(byLevel2.get(employee.level2Code()), Position.DIVISION_MANAGER.getJapaneseName())
                                .ifPresent(approvers::add);
                        }
                        break;
                        
                    case DIVISION_MANAGER:
                        // 同じ統括本部の統括本部長を探す
                        if (employee.level1Code() != null) {
                            findApproverByPosition(byLevel1.get(employee.level1Code()), Position.GENERAL_MANAGER.getJapaneseName())
                                .ifPresent(approvers::add);
                        }
                        break;
                        
                    case GENERAL_MANAGER:
                        // 統括本部長には承認者なし
                        break;
                }
                
            } catch (IllegalArgumentException e) {
                // 未知の役職の場合はスキップ
                log.warn("Unknown position '{}' for employee '{}', skipping approver calculation", 
                        employee.position(), employee.email());
                continue;
            }
            
            approverRelations.put(employee.email(), approvers);
        }
        
        log.debug("Approver calculation completed. {} employees processed, {} approval relationships established",
                 allEmployees.size(), 
                 approverRelations.values().stream().mapToInt(Set::size).sum());
        
        return approverRelations;
    }
    
    /**
     * コレクションを指定されたキー抽出関数でグルーピング
     * nullキーは除外される
     * 
     * @param items グルーピング対象のアイテム
     * @param keyExtractor キー抽出関数
     * @return グルーピング結果のマップ
     */
    private <T> Map<String, List<T>> groupBy(Collection<T> items, Function<T, String> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return new HashMap<>();
        }
        
        return items.stream()
                   .filter(item -> item != null && keyExtractor.apply(item) != null)
                   .collect(Collectors.groupingBy(keyExtractor));
    }
    
    /**
     * 指定されたポジションの承認者候補を検索
     * 候補リストの中から指定されたポジション（日本語名）に一致する最初の従業員のメールアドレスを返す
     * 
     * @param candidates 承認者候補のリスト
     * @param position 検索対象のポジション（日本語名）
     * @return 承認者のメールアドレス（見つからない場合はOptional.empty()）
     */
    private Optional<String> findApproverByPosition(List<EmployeeRecord> candidates, String position) {
        if (candidates == null || candidates.isEmpty() || position == null) {
            return Optional.empty();
        }
        
        return candidates.stream()
                        .filter(candidate -> position.equals(candidate.position()))
                        .map(EmployeeRecord::email)
                        .findFirst();
    }
}