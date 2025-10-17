package com.devhour.application.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devhour.application.dto.BatchResult;
import com.devhour.application.exception.BatchProcessingException;
import com.devhour.application.exception.RecordValidationException;
import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.entity.Approver;
import com.devhour.domain.model.valueobject.EmployeeRecord;
import com.devhour.domain.model.valueobject.Position;
import com.devhour.domain.repository.ApprovalAuthorityRepository;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.domain.service.OrganizationHierarchyProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 承認権限バッチサービス
 * 
 * 社員マスタCSVから承認権限者の情報を抽出・登録し、
 * 組織階層に基づいて承認者関係を自動設定するバッチ処理サービス
 * 
 * 主要機能:
 * - CSVファイルの解析と検証
 * - 承認権限者（マネージャー以上）の抽出
 * - 組織階層処理による承認者関係の計算
 * - 差分更新による効率的なデータ更新
 * - エラー処理と回復機能
 * - トランザクション管理
 * 
 * CSVフォーマット:
 * メールアドレス,氏名,最上位の組織コード,最上位の組織名,２階層目の組織コード,２階層目の組織名,３階層目の組織コード,３階層目の組織名,４階層目の組織コード,４階層目の組織名,役職
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalAuthorityBatchService {
    
    private final ApprovalAuthorityRepository approvalAuthorityRepository;
    private final ApproverRepository approverRepository;
    private final OrganizationHierarchyProcessor hierarchyProcessor;
    
    /**
     * ファイルパスから承認権限者をインポート（スケジューラー用）
     * 
     * @param filePath CSVファイルのパス
     * @return バッチ処理結果
     */
    public BatchResult importApprovalAuthoritiesFromFile(String filePath) {
        long startTime = System.currentTimeMillis();
        log.info("=== 承認権限バッチ処理開始 ===");
        log.info("処理対象ファイル: {}", filePath);
        
        BatchResult result = new BatchResult();
        
        try {
            ParseResult parseResult = parseCSV(filePath);
            log.info("CSV解析完了 - 総レコード数: {} 件", parseResult.getRecords().size());
            
            // CSV解析エラーを結果に追加
            result.addErrors(parseResult.getErrors());
            
            if (!parseResult.getRecords().isEmpty()) {
                // 役職別集計をログ出力
                logPositionStatistics(parseResult.getRecords());
                
                BatchResult processingResult = updateApprovalAuthoritiesWithApprovers(parseResult.getRecords());
                
                // 結果をマージ
                result.setProcessed(processingResult.getProcessed());
                result.setAdded(processingResult.getAdded());
                result.setUpdated(processingResult.getUpdated());
                result.setDeleted(processingResult.getDeleted());
                result.setApproverRelations(processingResult.getApproverRelations());
                result.addErrors(processingResult.getErrors());
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("=== 承認権限バッチ処理完了 ===");
            log.info("処理時間: {}ms", processingTime);
            logBatchResults(result);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("=== 承認権限バッチ処理失敗 ===");
            log.error("処理時間: {}ms", processingTime);
            log.error("エラー内容", e);
            result.addError("File import failed: " + e.getMessage());
        } finally {
            result.markCompleted();
        }
        
        return result;
    }
    
    /**
     * バッチ処理結果の詳細ログ出力
     * 
     * @param result バッチ処理結果
     */
    private void logBatchResults(BatchResult result) {
        log.info("--- 処理結果サマリー ---");
        log.info("処理済み: {} 件", result.getProcessed());
        log.info("追加: {} 件", result.getAdded());
        log.info("更新: {} 件", result.getUpdated());
        log.info("削除: {} 件", result.getDeleted());
        log.info("承認者関係 - 追加: {} 件", result.getApproverRelations().getAdded());
        log.info("承認者関係 - 削除: {} 件", result.getApproverRelations().getDeleted());
        
        if (!result.getErrors().isEmpty()) {
            log.warn("エラー件数: {} 件", result.getErrors().size());
            result.getErrors().forEach(error -> log.warn("エラー詳細: {}", error));
        }
    }
    
    /**
     * 役職別集計のログ出力
     * 
     * @param records 従業員レコードのリスト
     */
    private void logPositionStatistics(List<EmployeeRecord> records) {
        Map<String, Long> positionCounts = records.stream()
            .collect(Collectors.groupingBy(EmployeeRecord::position, Collectors.counting()));
        
        log.info("--- 役職別集計 ---");
        positionCounts.forEach((position, count) -> 
            log.info("{}: {} 名", position, count));
        
        long approvalAuthorityCount = records.stream()
            .mapToLong(record -> isApprovalPosition(record.position()) ? 1 : 0)
            .sum();
        log.info("承認権限者総数: {} 名", approvalAuthorityCount);
    }
    
    /**
     * CSVファイルを解析してEmployeeRecordのリストを作成（検証強化版）
     * 
     * @param filePath CSVファイルのパス
     * @return 解析結果（従業員レコードのリストとエラーリスト）
     * @throws IOException ファイル読み込みエラー
     */
    private ParseResult parseCSV(String filePath) throws IOException {
        log.info("CSVファイル検証・解析開始: {}", filePath);
        
        try {
            return parseCSVWithValidation(filePath);
        } catch (BatchProcessingException e) {
            log.error("CSVファイル検証エラー: {}", e.getMessage());
            throw new RuntimeException("CSVファイル検証に失敗しました: " + filePath, e);
        } catch (Exception e) {
            log.error("CSVファイル解析エラー: {}", filePath, e);
            throw new RuntimeException("CSVファイル解析に失敗しました: " + filePath, e);
        }
    }
    
    /**
     * CSVファイル解析（エラー処理強化版）
     */
    private ParseResult parseCSVWithValidation(String filePath) throws BatchProcessingException {
        // File access validation
        validateFileAccess(filePath);
        
        List<EmployeeRecord> validRecords = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();
        int totalLines = 0;
        int validLines = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            // Header validation (optional header support)
            boolean hasHeader = false;
            if ((line = reader.readLine()) != null) {
                lineNumber++;
                totalLines++;
                hasHeader = validateCsvHeader(line);
                if (!hasHeader) {
                    // 先頭行がデータの場合、そのまま処理対象として扱う
                    if (line.trim().isEmpty()) {
                        log.debug("行{}: 空行をスキップ", lineNumber);
                    } else {
                        try {
                            EmployeeRecord record = validateAndParseRecord(line, lineNumber);
                            validRecords.add(record);
                            validLines++;
                        } catch (RecordValidationException e) {
                            validationErrors.add(String.format("行%d: %s", lineNumber, e.getMessage()));
                            log.warn("行{}の検証エラー: {}", lineNumber, e.getMessage());
                        }
                    }
                }
            } else {
                throw new BatchProcessingException("CSVファイルが空です");
            }
            
            // Data lines processing
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalLines++;
                
                if (line.trim().isEmpty()) {
                    log.debug("行{}: 空行をスキップ", lineNumber);
                    continue;
                }
                
                try {
                    EmployeeRecord record = validateAndParseRecord(line, lineNumber);
                    validRecords.add(record);
                    validLines++;
                } catch (RecordValidationException e) {
                    validationErrors.add(String.format("行%d: %s", lineNumber, e.getMessage()));
                    log.warn("行{}の検証エラー: {}", lineNumber, e.getMessage());
                }
            }
            
            // Validation result logging
            log.info("CSV解析完了 - 総行数: {}, 有効行数: {}, エラー行数: {}", 
                    totalLines, validLines, validationErrors.size());
                    
            // Error threshold check (fail if more than 10% errors)
            double errorRate = (double) validationErrors.size() / Math.max(1, totalLines - 1);
            if (errorRate > 0.1) {
                log.error("エラー率が閾値を超過しました: {:.1f}% (閾値: 10%)", errorRate * 100);
                throw new BatchProcessingException(
                    String.format("CSVファイルのエラー率が高すぎます: %.1f%% (%d/%d行)", 
                                 errorRate * 100, validationErrors.size(), totalLines - 1));
            }
            
            if (!validationErrors.isEmpty()) {
                log.warn("以下の行で検証エラーが発生しました:");
                validationErrors.forEach(error -> log.warn("  {}", error));
            }
            
            return new ParseResult(validRecords, validationErrors);
            
        } catch (IOException e) {
            log.error("CSVファイル読み取りエラー: {}", filePath, e);
            throw new BatchProcessingException("CSVファイル読み取りに失敗しました: " + filePath, e);
        }
    }
    
    /**
     * CSV解析結果を表すクラス
     */
    private static class ParseResult {
        private final List<EmployeeRecord> records;
        private final List<String> errors;
        
        public ParseResult(List<EmployeeRecord> records, List<String> errors) {
            this.records = records;
            this.errors = errors;
        }
        
        public List<EmployeeRecord> getRecords() { return records; }
        public List<String> getErrors() { return errors; }
    }
    
    /**
     * CSVファイルの存在確認とアクセス権チェック
     */
    private void validateFileAccess(String filePath) throws BatchProcessingException {
        File csvFile = new File(filePath);
        
        if (!csvFile.exists()) {
            log.error("CSVファイルが存在しません: {}", filePath);
            throw new BatchProcessingException("CSVファイルが見つかりません: " + filePath);
        }
        
        if (!csvFile.canRead()) {
            log.error("CSVファイルの読み取り権限がありません: {}", filePath);
            throw new BatchProcessingException("CSVファイルの読み取り権限がありません: " + filePath);
        }
        
        if (csvFile.length() == 0) {
            log.error("CSVファイルが空です: {}", filePath);
            throw new BatchProcessingException("CSVファイルが空です: " + filePath);
        }
        
        log.info("CSVファイル検証成功 - サイズ: {} bytes", csvFile.length());
    }
    
    /**
     * CSVヘッダーのフォーマット検証
     * 先頭行がヘッダーかデータかを自動判定し、ヘッダーの場合のみ検証
     */
    private boolean validateCsvHeader(String firstLine) throws BatchProcessingException {
        String[] expectedHeaders = {
            "メールアドレス", "氏名", 
            "最上位の組織コード", "最上位の組織名",
            "２階層目の組織コード", "２階層目の組織名", 
            "３階層目の組織コード", "３階層目の組織名",
            "４階層目の組織コード", "４階層目の組織名",
            "役職"
        };
        
        String[] actualFields = firstLine.split(",");
        
        // 先頭行がヘッダーかデータかを判定
        // ヘッダーの場合、最初のフィールドは "メールアドレス" で始まる
        // データの場合、最初のフィールドはメールアドレス形式（@を含む）
        boolean isHeader = actualFields.length > 0 && 
                          actualFields[0].trim().equals("メールアドレス");
        
        if (!isHeader) {
            log.debug("先頭行はデータ行と判定（ヘッダーなし）");
            return false; // ヘッダーではない
        }
        
        // ヘッダー行の場合は検証を実行
        if (actualFields.length != expectedHeaders.length) {
            log.error("CSVヘッダーの列数が不正です。期待値: {}, 実際: {}", 
                     expectedHeaders.length, actualFields.length);
            throw new BatchProcessingException(
                String.format("CSVヘッダーの列数が不正です。期待値: %d列, 実際: %d列", 
                             expectedHeaders.length, actualFields.length));
        }
        
        for (int i = 0; i < expectedHeaders.length; i++) {
            if (!expectedHeaders[i].equals(actualFields[i].trim())) {
                log.error("CSVヘッダーが不正です。列{}: 期待値='{}', 実際='{}'", 
                         i + 1, expectedHeaders[i], actualFields[i].trim());
                throw new BatchProcessingException(
                    String.format("CSVヘッダーが不正です。列%d: 期待値='%s', 実際='%s'", 
                                 i + 1, expectedHeaders[i], actualFields[i].trim()));
            }
        }
        
        log.debug("CSVヘッダー検証成功");
        return true; // ヘッダーあり
    }
    
    /**
     * 個別レコードのデータ検証
     */
    private EmployeeRecord validateAndParseRecord(String line, int lineNumber) throws RecordValidationException {
        try {
            String[] fields = line.split(",", -1); // -1 to include empty trailing fields
            
            if (fields.length != 11) {
                throw new RecordValidationException(
                    String.format("列数が不正です。期待値: 11列, 実際: %d列", fields.length));
            }
            
            // Required field validation
            String email = validateEmail(fields[0].trim(), lineNumber);
            String name = validateName(fields[1].trim(), lineNumber);
            String position = validatePosition(fields[10].trim(), lineNumber);
            
            // Create EmployeeRecord with validated data
            return new EmployeeRecord(
                email, name, position,
                trimOrNull(fields[2]), trimOrNull(fields[3]),
                trimOrNull(fields[4]), trimOrNull(fields[5]),
                trimOrNull(fields[6]), trimOrNull(fields[7]),
                trimOrNull(fields[8]), trimOrNull(fields[9])
            );
            
        } catch (RecordValidationException e) {
            throw e; // Re-throw validation exceptions as-is
        } catch (Exception e) {
            log.warn("行{}の解析エラー: {}", lineNumber, e.getMessage());
            throw new RecordValidationException(e.getMessage());
        }
    }
    
    /**
     * メールアドレス形式の検証
     */
    private String validateEmail(String email, int lineNumber) throws RecordValidationException {
        if (email.isEmpty()) {
            throw new RecordValidationException("メールアドレスが空です");
        }
        
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailPattern)) {
            throw new RecordValidationException("メールアドレスの形式が不正です: " + email);
        }
        
        return email;
    }
    
    /**
     * 氏名の検証
     */
    private String validateName(String name, int lineNumber) throws RecordValidationException {
        if (name.isEmpty()) {
            throw new RecordValidationException("氏名が空です");
        }
        
        if (name.length() > 255) {
            throw new RecordValidationException("氏名が長すぎます（255文字以内）: " + name);
        }
        
        return name;
    }
    
    /**
     * 役職の検証
     */
    private String validatePosition(String position, int lineNumber) throws RecordValidationException {
        if (position.isEmpty()) {
            throw new RecordValidationException("役職が空です");
        }
        
        try {
            Position.fromJapaneseName(position);
            return position;
        } catch (IllegalArgumentException e) {
            throw new RecordValidationException("不正な役職です: " + position);
        }
    }
    
    /**
     * 文字列をトリムし、空の場合はnullを返す
     * 
     * @param value 入力文字列
     * @return トリム後の文字列、空の場合はnull
     */
    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * null でない場合にトリムする
     */
    private String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }
    
    /**
     * 承認権限者と承認者関係の更新
     * 
     * @param records 従業員レコードのリスト
     * @return バッチ処理結果
     */
    private BatchResult updateApprovalAuthoritiesWithApprovers(List<EmployeeRecord> records) {
        BatchResult result = new BatchResult();
        
        try {
            // 1. 承認権限者（マネージャー以上）のみを抽出してメモリにロード
            Map<String, ApprovalAuthority> authorityMap = records.stream()
                .filter(r -> isApprovalPosition(r.position()))
                .collect(Collectors.toMap(
                    EmployeeRecord::email,
                    this::toApprovalAuthority,
                    (existing, replacement) -> replacement // 重複時は新しい方を優先
                ));
            
            log.debug("Extracted {} approval authorities from {} records", 
                     authorityMap.size(), records.size());
            
            // 2. 全社員情報をメモリにロードして承認関係を計算
            Map<String, EmployeeRecord> allEmployees = records.stream()
                .collect(Collectors.toMap(
                    EmployeeRecord::email,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));
            Map<String, Set<String>> approverRelations = calculateApproverRelations(allEmployees);
            
            // 3. 既存の承認権限者と承認者関係を取得
            List<ApprovalAuthority> existingAuthorities = approvalAuthorityRepository.findAll();
            Map<String, Set<String>> existingApprovers = approverRepository.findAllGroupedByTarget();
            
            // 既存データのマップ化
            Map<String, ApprovalAuthority> existingAuthorityMap = existingAuthorities.stream()
                .collect(Collectors.toMap(ApprovalAuthority::getEmail, Function.identity()));
            
            log.debug("Found {} existing authorities and {} existing approver relationships",
                     existingAuthorities.size(), existingApprovers.size());
            
            // 4. 承認権限者毎にトランザクション処理
            for (String authorityEmail : authorityMap.keySet()) {
                processAuthorityInTransaction(
                    authorityEmail,
                    authorityMap.get(authorityEmail),
                    approverRelations.getOrDefault(authorityEmail, Collections.emptySet()),
                    existingApprovers.getOrDefault(authorityEmail, Collections.emptySet()),
                    existingAuthorityMap.containsKey(authorityEmail),
                    result
                );
            }
            
            // 5. 削除された承認権限者の処理
            processDeletedAuthorities(existingAuthorityMap, authorityMap, result);
            
        } catch (Exception e) {
            log.error("Error during approval authorities update", e);
            result.addError("Batch processing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 組織階層処理のログ出力付き承認者関係計算
     * 
     * @param allEmployees 全従業員マップ
     * @return 承認者関係マップ
     */
    private Map<String, Set<String>> calculateApproverRelations(Map<String, EmployeeRecord> allEmployees) {
        log.info("組織階層処理開始 - 対象者数: {} 名", allEmployees.size());
        
        Map<String, Set<String>> relations = hierarchyProcessor.calculateApprovers(allEmployees);
        
        int totalRelations = relations.values().stream()
            .mapToInt(Set::size)
            .sum();
        
        log.info("組織階層処理完了 - 承認者関係総数: {} 件", totalRelations);
        log.debug("Calculated approver relations for {} employees", relations.size());
        
        return relations;
    }

    /**
     * 承認権限を持つ役職かチェック
     * 
     * @param positionName 役職名（日本語）
     * @return 承認権限がある場合true
     */
    private boolean isApprovalPosition(String positionName) {
        try {
            Position position = Position.fromJapaneseName(positionName);
            return position.hasApprovalAuthority();
        } catch (IllegalArgumentException e) {
            log.warn("Unknown position: {}", positionName);
            return false;
        }
    }
    
    /**
     * EmployeeRecordをApprovalAuthorityエンティティに変換
     * 
     * @param record 従業員レコード
     * @return 承認権限エンティティ
     */
    private ApprovalAuthority toApprovalAuthority(EmployeeRecord record) {
        try {
            return record.toApprovalAuthority();
        } catch (Exception e) {
            log.error("Failed to convert EmployeeRecord to ApprovalAuthority: {}", record, e);
            throw new RuntimeException("Entity conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 承認権限者毎のトランザクション処理（ログ強化）
     * 
     * @param authorityEmail 承認権限者のメールアドレス
     * @param authority 承認権限エンティティ
     * @param newApprovers 新しい承認者のセット
     * @param existingApprovers 既存の承認者のセット
     * @param isUpdate 既存レコードの更新か新規作成か
     * @param result バッチ処理結果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processAuthorityInTransaction(
        String authorityEmail,
        ApprovalAuthority authority,
        Set<String> newApprovers,
        Set<String> existingApprovers,
        boolean isUpdate,
        BatchResult result
    ) {
        try {
            log.debug("承認権限者処理開始: {}", authorityEmail);
            
            // 承認権限者の更新
            approvalAuthorityRepository.save(authority);
            result.incrementProcessed();
            
            if (isUpdate) {
                result.incrementUpdated();
            } else {
                result.incrementAdded();
            }
            
            // 承認者関係の差分更新前の状態を記録
            int beforeAdded = result.getApproverRelations().getAdded();
            int beforeDeleted = result.getApproverRelations().getDeleted();
            
            // 承認者関係の差分更新
            updateApproverRelationships(authorityEmail, newApprovers, existingApprovers, result);
            
            // 処理後の変更数を計算
            int addedCount = result.getApproverRelations().getAdded() - beforeAdded;
            int deletedCount = result.getApproverRelations().getDeleted() - beforeDeleted;
            
            log.debug("承認権限者処理完了: {} - 承認者関係 追加:{} 削除:{}", 
                     authorityEmail, addedCount, deletedCount);
            
        } catch (Exception e) {
            log.error("承認権限者処理エラー: {}", authorityEmail, e);
            result.addError("承認権限者処理失敗 [" + authorityEmail + "]: " + e.getMessage());
            // Note: Don't re-throw the exception to allow other authorities to be processed
        }
    }
    
    /**
     * 承認者関係の差分更新
     * 
     * @param targetEmail 対象者メールアドレス
     * @param newApprovers 新しい承認者のセット
     * @param existingApprovers 既存の承認者のセット
     * @param result バッチ処理結果
     */
    private void updateApproverRelationships(
        String targetEmail,
        Set<String> newApprovers,
        Set<String> existingApprovers,
        BatchResult result
    ) {
        // 追加する承認者
        Set<String> toAdd = new HashSet<>(newApprovers);
        toAdd.removeAll(existingApprovers);
        
        // 削除する承認者
        Set<String> toDelete = new HashSet<>(existingApprovers);
        toDelete.removeAll(newApprovers);
        
        log.debug("Approver relationships for {}: adding {}, deleting {}", 
                 targetEmail, toAdd.size(), toDelete.size());
        
        // 削除処理
        for (String approverEmail : toDelete) {
            try {
                approverRepository.deleteByTargetAndApprover(targetEmail, approverEmail);
                result.incrementApproverDeleted();
                log.debug("Deleted approver relationship: {} -> {}", targetEmail, approverEmail);
            } catch (Exception e) {
                log.error("Failed to delete approver relationship: {} -> {}", targetEmail, approverEmail, e);
                result.addError(String.format("Failed to delete approver relationship: %s -> %s - %s", 
                               targetEmail, approverEmail, e.getMessage()));
            }
        }
        
        // 追加処理
        for (String approverEmail : toAdd) {
            try {
                Approver relation = Approver.create(
                    targetEmail,
                    approverEmail,
                    LocalDateTime.now(),
                    null // 終了日時はnull（無期限）
                );
                approverRepository.save(relation);
                result.incrementApproverAdded();
                log.debug("Added approver relationship: {} -> {}", targetEmail, approverEmail);
            } catch (Exception e) {
                log.error("Failed to add approver relationship: {} -> {}", targetEmail, approverEmail, e);
                result.addError(String.format("Failed to add approver relationship: %s -> %s - %s", 
                               targetEmail, approverEmail, e.getMessage()));
            }
        }
    }
    
    /**
     * 削除された承認権限者の処理
     * 
     * @param existingAuthorityMap 既存の承認権限者マップ
     * @param newAuthorityMap 新しい承認権限者マップ
     * @param result バッチ処理結果
     */
    private void processDeletedAuthorities(
        Map<String, ApprovalAuthority> existingAuthorityMap,
        Map<String, ApprovalAuthority> newAuthorityMap,
        BatchResult result
    ) {
        Set<String> deletedEmails = existingAuthorityMap.keySet().stream()
            .filter(email -> !newAuthorityMap.containsKey(email))
            .collect(Collectors.toSet());
        
        log.debug("Found {} deleted authorities", deletedEmails.size());
        
        for (String email : deletedEmails) {
            deleteAuthorityInTransaction(email, result);
        }
    }
    
    /**
     * 承認権限者削除のトランザクション処理
     * 
     * @param email 削除対象のメールアドレス
     * @param result バッチ処理結果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteAuthorityInTransaction(String email, BatchResult result) {
        try {
            log.debug("Deleting authority: {}", email);
            
            // 承認者関係も含めて削除（cascade的な処理）
            approvalAuthorityRepository.deleteByEmail(email);
            result.incrementDeleted();
            
            log.debug("Successfully deleted authority: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to delete authority: {}", email, e);
            result.addError("Failed to delete authority: " + email + " - " + e.getMessage());
        }
    }
}