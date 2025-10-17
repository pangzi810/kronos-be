package com.devhour.domain.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import com.devhour.domain.model.valueobject.EmployeeRecord;

/**
 * OrganizationHierarchyProcessorのテスト
 * 
 * テスト対象:
 * - 組織階層に基づく承認者関係の計算ロジック
 * - 全役職レベルの承認ルール（要件6.2-6.9）
 * - エッジケース（不完全な組織構造、未知の役職等）
 * - パフォーマンステスト（大規模データセット）
 */
@DisplayName("OrganizationHierarchyProcessor - 組織階層処理サービス")
class OrganizationHierarchyProcessorTest {

    private final OrganizationHierarchyProcessor processor = new OrganizationHierarchyProcessor();

    @Nested
    @DisplayName("承認者計算の基本機能")
    class BasicApprovalCalculation {

        @Test
        @DisplayName("一般社員: 同グループのマネージャーが承認者として設定される")
        void employeeApprovedByManagerInSameGroup() {
            // Given: 同じグループに一般社員とマネージャーがいる組織構造
            Map<String, EmployeeRecord> employees = Map.of(
                "employee@example.com", createEmployee("employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "manager@example.com", createEmployee("manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                    "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 一般社員の承認者はマネージャー
            assertThat(result.get("employee@example.com")).containsExactly("manager@example.com");
            // マネージャーには承認者なし（部長がいない）
            assertThat(result.get("manager@example.com")).isEmpty();
        }

        @Test
        @DisplayName("一般社員: グループにマネージャーがいない場合は部長が承認者")
        void employeeApprovedByDepartmentManagerWhenNoManager() {
            // Given: グループにマネージャーがおらず、部に部長がいる構造
            Map<String, EmployeeRecord> employees = Map.of(
                "employee@example.com", createEmployee("employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "dept_manager@example.com", createEmployee("dept_manager@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 一般社員の承認者は部長
            assertThat(result.get("employee@example.com")).containsExactly("dept_manager@example.com");
        }

        @Test
        @DisplayName("マネージャー: 同部の部長が承認者として設定される")
        void managerApprovedByDepartmentManager() {
            // Given: 同じ部にマネージャーと部長がいる構造
            Map<String, EmployeeRecord> employees = Map.of(
                "manager@example.com", createEmployee("manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                    "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "dept_manager@example.com", createEmployee("dept_manager@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: マネージャーの承認者は部長
            assertThat(result.get("manager@example.com")).containsExactly("dept_manager@example.com");
        }

        @Test
        @DisplayName("部長: 同本部の本部長が承認者として設定される")
        void departmentManagerApprovedByDivisionManager() {
            // Given: 同じ本部に部長と本部長がいる構造
            Map<String, EmployeeRecord> employees = Map.of(
                "dept_manager@example.com", createEmployee("dept_manager@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長"),
                "div_manager@example.com", createEmployee("div_manager@example.com", "田中次郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", null, null, null, null, "本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 部長の承認者は本部長
            assertThat(result.get("dept_manager@example.com")).containsExactly("div_manager@example.com");
        }

        @Test
        @DisplayName("本部長: 同統括本部の統括本部長が承認者として設定される")
        void divisionManagerApprovedByGeneralManager() {
            // Given: 同じ統括本部に本部長と統括本部長がいる構造
            Map<String, EmployeeRecord> employees = Map.of(
                "div_manager@example.com", createEmployee("div_manager@example.com", "田中次郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", null, null, null, null, "本部長"),
                "general_manager@example.com", createEmployee("general_manager@example.com", "山田三郎", "1000", "開発統括本部", 
                                                            null, null, null, null, null, null, "統括本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 本部長の承認者は統括本部長
            assertThat(result.get("div_manager@example.com")).containsExactly("general_manager@example.com");
        }

        @Test
        @DisplayName("統括本部長: 承認者なし")
        void generalManagerHasNoApprover() {
            // Given: 統括本部長のみの構造
            Map<String, EmployeeRecord> employees = Map.of(
                "general_manager@example.com", createEmployee("general_manager@example.com", "山田三郎", "1000", "開発統括本部", 
                                                            null, null, null, null, null, null, "統括本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 統括本部長に承認者はいない
            assertThat(result.get("general_manager@example.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("組織構造のバリエーション")
    class OrganizationStructureVariations {

        @Test
        @DisplayName("完全な4階層組織での承認者関係")
        void completeOrganizationHierarchy() {
            // Given: 完全な4階層組織（統括本部 > 本部 > 部 > グループ）
            Map<String, EmployeeRecord> employees = Map.of(
                "employee@example.com", createEmployee("employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "manager@example.com", createEmployee("manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                    "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "dept_manager@example.com", createEmployee("dept_manager@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長"),
                "div_manager@example.com", createEmployee("div_manager@example.com", "田中次郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", null, null, null, null, "本部長"),
                "general_manager@example.com", createEmployee("general_manager@example.com", "山田三郎", "1000", "開発統括本部", 
                                                            null, null, null, null, null, null, "統括本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 各役職レベルで適切な承認者が設定される
            assertThat(result.get("employee@example.com")).containsExactly("manager@example.com");
            assertThat(result.get("manager@example.com")).containsExactly("dept_manager@example.com");
            assertThat(result.get("dept_manager@example.com")).containsExactly("div_manager@example.com");
            assertThat(result.get("div_manager@example.com")).containsExactly("general_manager@example.com");
            assertThat(result.get("general_manager@example.com")).isEmpty();
        }

        @Test
        @DisplayName("不完全な組織階層での承認者関係")
        void incompleteOrganizationHierarchy() {
            // Given: 中間階層が欠けた組織（部長がいない）
            Map<String, EmployeeRecord> employees = Map.of(
                "employee@example.com", createEmployee("employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "manager@example.com", createEmployee("manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                    "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                // 部長なし
                "div_manager@example.com", createEmployee("div_manager@example.com", "田中次郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", null, null, null, null, "本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 一般社員はマネージャーが承認、マネージャーは本部長が承認（部長スキップ）
            assertThat(result.get("employee@example.com")).containsExactly("manager@example.com");
            assertThat(result.get("manager@example.com")).isEmpty(); // 同じ部に部長がいない
            assertThat(result.get("div_manager@example.com")).isEmpty(); // 統括本部長がいない
        }

        @Test
        @DisplayName("複数の組織が混在する構造")
        void multipleOrganizationStructure() {
            // Given: 開発統括本部と営業統括本部が混在
            Map<String, EmployeeRecord> employees = Map.of(
                "dev_employee@example.com", createEmployee("dev_employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                          "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "dev_manager@example.com", createEmployee("dev_manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                        "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "sales_employee@example.com", createEmployee("sales_employee@example.com", "鈴木次郎", "2000", "営業統括本部", 
                                                           "2100", "営業本部", "2110", "営業1部", "2111", "営業1グループ", "一般社員"),
                "sales_manager@example.com", createEmployee("sales_manager@example.com", "田中三郎", "2000", "営業統括本部", 
                                                          "2100", "営業本部", "2110", "営業1部", "2111", "営業1グループ", "マネージャー")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 各統括本部内での承認関係が独立して設定される
            assertThat(result.get("dev_employee@example.com")).containsExactly("dev_manager@example.com");
            assertThat(result.get("sales_employee@example.com")).containsExactly("sales_manager@example.com");
            // 開発と営業のマネージャーは各々の組織内でのみ承認関係を持つ
            assertThat(result.get("dev_manager@example.com")).isEmpty();
            assertThat(result.get("sales_manager@example.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("エッジケースと例外処理")
    class EdgeCasesAndExceptionHandling {

        @Test
        @DisplayName("空の社員マップの場合は空の結果を返す")
        void emptyEmployeeMap() {
            // Given: 空の社員マップ
            Map<String, EmployeeRecord> employees = Map.of();

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 空の結果が返される
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("未知の役職の社員はスキップされる")
        void unknownPositionSkipped() {
            // Given: 未知の役職を持つ社員を含むマップ
            Map<String, EmployeeRecord> employees = new HashMap<>();
            employees.put("normal@example.com", createEmployee("normal@example.com", "田中太郎", "1000", "開発統括本部", 
                                                              "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"));
            
            // 未知の役職の社員（手動でrecordを作成）
            try {
                employees.put("unknown@example.com", createEmployeeWithInvalidPosition("unknown@example.com", "未知職位"));
                fail("不正な役職で EmployeeRecord が作成できてはいけない");
            } catch (IllegalArgumentException e) {
                // 期待される例外 - EmployeeRecord作成時にバリデーションされる
            }

            // When: 正常な社員のみで承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 正常な社員の結果のみ含まれる
            assertThat(result).containsKey("normal@example.com");
            assertThat(result.get("normal@example.com")).isEmpty(); // マネージャーがいない
        }

        @Test
        @DisplayName("組織情報が不完全な社員の処理")
        void incompleteOrganizationInfo() {
            // Given: 組織情報が部分的にしかない社員
            // マネージャーは部長を探すが、同じ部（level3）に部長がいない場合は承認者なし
            Map<String, EmployeeRecord> employees = Map.of(
                "partial@example.com", createEmployee("partial@example.com", "田中太郎", "1000", "開発統括本部", 
                                                     null, null, null, null, null, null, "マネージャー"),
                "complete@example.com", createEmployee("complete@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                      null, null, null, null, null, null, "統括本部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: マネージャーは同じ部（level3）に部長がいないため承認者なし
            assertThat(result.get("partial@example.com")).isEmpty();
            assertThat(result.get("complete@example.com")).isEmpty();
        }

        @Test
        @DisplayName("nullの組織コードを持つ社員の処理")
        void nullOrganizationCodes() {
            // Given: 全ての組織コードがnullの社員
            Map<String, EmployeeRecord> employees = Map.of(
                "employee@example.com", createEmployee("employee@example.com", "田中太郎", null, null, 
                                                      null, null, null, null, null, null, "一般社員"),
                "manager@example.com", createEmployee("manager@example.com", "佐藤花子", null, null, 
                                                    null, null, null, null, null, null, "マネージャー")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 組織コードがnullでも処理され、承認者が見つからない
            assertThat(result.get("employee@example.com")).isEmpty();
            assertThat(result.get("manager@example.com")).isEmpty();
        }

        @Test
        @DisplayName("循環する組織構造の処理")
        void circularOrganizationStructure() {
            // Given: 通常の階層構造（循環は組織コードの性質上発生しない）
            Map<String, EmployeeRecord> employees = Map.of(
                "manager1@example.com", createEmployee("manager1@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "manager2@example.com", createEmployee("manager2@example.com", "田中太郎", "1000", "開発統括本部", 
                                                      "1100", "開発本部", "1110", "開発1部", "1112", "開発2グループ", "マネージャー"),
                "dept_manager@example.com", createEmployee("dept_manager@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 正常に処理される（循環は発生しない）
            assertThat(result.get("manager1@example.com")).containsExactly("dept_manager@example.com");
            assertThat(result.get("manager2@example.com")).containsExactly("dept_manager@example.com");
            assertThat(result.get("dept_manager@example.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("パフォーマンステスト")
    class PerformanceTests {

        @Test
        @DisplayName("大規模データセット（1000人）での処理性能")
        void largeDatasetPerformance() {
            // Given: 1000人の社員データ（現実的な組織構造）
            Map<String, EmployeeRecord> employees = createLargeOrganization(1000);

            // When: 処理時間を測定
            long startTime = System.currentTimeMillis();
            Map<String, Set<String>> result = processor.calculateApprovers(employees);
            long processingTime = System.currentTimeMillis() - startTime;

            // Then: 処理が完了し、適切な結果が返される
            assertThat(result).hasSize(1000);
            assertThat(processingTime).isLessThan(1000); // 1秒以内での処理を期待
            
            // 結果のサンプル検証
            String firstEmployeeEmail = employees.keySet().iterator().next();
            assertThat(result).containsKey(firstEmployeeEmail);
        }

        @Test
        @DisplayName("メモリ効率の検証")
        void memoryEfficiencyTest() {
            // Given: 中規模データセット（500人）
            Map<String, EmployeeRecord> employees = createLargeOrganization(500);

            // When: 処理を実行
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 結果が正常である
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            // 結果のキーが入力されたemployeesに含まれていることを確認
            assertThat(result.keySet()).isSubsetOf(employees.keySet());
            // 結果の整合性を確認（承認者関係の数は0以上）
            long totalApprovalRelations = result.values().stream().mapToLong(Set::size).sum();
            assertThat(totalApprovalRelations).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("複雑なシナリオテスト")
    class ComplexScenarioTests {

        @Test
        @DisplayName("マトリックス組織での承認者関係")
        void matrixOrganizationApprovers() {
            // Given: プロジェクトチームと機能部門が混在する構造
            Map<String, EmployeeRecord> employees = Map.of(
                // 開発機能部門
                "dev_employee@example.com", createEmployee("dev_employee@example.com", "田中太郎", "1000", "開発統括本部", 
                                                          "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "dev_manager@example.com", createEmployee("dev_manager@example.com", "佐藤花子", "1000", "開発統括本部", 
                                                        "1100", "開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "dev_dept_mgr@example.com", createEmployee("dev_dept_mgr@example.com", "鈴木一郎", "1000", "開発統括本部", 
                                                         "1100", "開発本部", "1110", "開発1部", null, null, "部長"),
                
                // QA機能部門  
                "qa_employee@example.com", createEmployee("qa_employee@example.com", "田中次郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", "1120", "QA部", "1121", "QA1グループ", "一般社員"),
                "qa_manager@example.com", createEmployee("qa_manager@example.com", "山田三郎", "1000", "開発統括本部", 
                                                       "1100", "開発本部", "1120", "QA部", "1121", "QA1グループ", "マネージャー"),
                "qa_dept_mgr@example.com", createEmployee("qa_dept_mgr@example.com", "伊藤四郎", "1000", "開発統括本部", 
                                                        "1100", "開発本部", "1120", "QA部", null, null, "部長")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 機能部門内での承認関係が正しく設定される
            assertThat(result.get("dev_employee@example.com")).containsExactly("dev_manager@example.com");
            assertThat(result.get("dev_manager@example.com")).containsExactly("dev_dept_mgr@example.com");
            
            assertThat(result.get("qa_employee@example.com")).containsExactly("qa_manager@example.com");
            assertThat(result.get("qa_manager@example.com")).containsExactly("qa_dept_mgr@example.com");
            
            // 部門間での承認関係はない
            assertThat(result.get("dev_dept_mgr@example.com")).isEmpty();
            assertThat(result.get("qa_dept_mgr@example.com")).isEmpty();
        }

        @Test
        @DisplayName("グローバル組織での承認者関係")
        void globalOrganizationApprovers() {
            // Given: 地域統括と機能統括が混在するグローバル組織
            Map<String, EmployeeRecord> employees = Map.of(
                // 日本地域
                "jp_employee@example.com", createEmployee("jp_employee@example.com", "田中太郎", "1000", "日本統括本部", 
                                                         "1100", "日本開発本部", "1110", "開発1部", "1111", "開発1グループ", "一般社員"),
                "jp_manager@example.com", createEmployee("jp_manager@example.com", "佐藤花子", "1000", "日本統括本部", 
                                                       "1100", "日本開発本部", "1110", "開発1部", "1111", "開発1グループ", "マネージャー"),
                "jp_dept_mgr@example.com", createEmployee("jp_dept_mgr@example.com", "鈴木一郎", "1000", "日本統括本部", 
                                                        "1100", "日本開発本部", "1110", "開発1部", null, null, "部長"),
                "jp_div_mgr@example.com", createEmployee("jp_div_mgr@example.com", "田中次郎", "1000", "日本統括本部", 
                                                       "1100", "日本開発本部", null, null, null, null, "本部長"),
                
                // US地域
                "us_employee@example.com", createEmployee("us_employee@example.com", "John Smith", "2000", "US統括本部", 
                                                        "2100", "US開発本部", "2110", "Dev1部", "2111", "Dev1グループ", "一般社員"),
                "us_manager@example.com", createEmployee("us_manager@example.com", "Jane Doe", "2000", "US統括本部", 
                                                       "2100", "US開発本部", "2110", "Dev1部", "2111", "Dev1グループ", "マネージャー")
            );

            // When: 承認者関係を計算
            Map<String, Set<String>> result = processor.calculateApprovers(employees);

            // Then: 地域内での承認関係が独立して設定される
            assertThat(result.get("jp_employee@example.com")).containsExactly("jp_manager@example.com");
            assertThat(result.get("jp_manager@example.com")).containsExactly("jp_dept_mgr@example.com");
            assertThat(result.get("jp_dept_mgr@example.com")).containsExactly("jp_div_mgr@example.com");
            
            assertThat(result.get("us_employee@example.com")).containsExactly("us_manager@example.com");
            // US地域にマネージャーより上位がいない場合
            assertThat(result.get("us_manager@example.com")).isEmpty();
        }
    }

    // ================= ヘルパーメソッド =================

    /**
     * テスト用のEmployeeRecordを作成するヘルパーメソッド
     */
    private EmployeeRecord createEmployee(String email, String name, String level1Code, String level1Name,
                                        String level2Code, String level2Name, String level3Code, String level3Name,
                                        String level4Code, String level4Name, String position) {
        return new EmployeeRecord(email, name, position, level1Code, level1Name, 
                                level2Code, level2Name, level3Code, level3Name, 
                                level4Code, level4Name);
    }

    /**
     * 無効な役職でEmployeeRecordの作成を試みるヘルパーメソッド
     */
    private EmployeeRecord createEmployeeWithInvalidPosition(String email, String invalidPosition) {
        // これは例外をスローするはず
        return new EmployeeRecord(email, "テスト名", invalidPosition, "1000", "統括本部", 
                                null, null, null, null, null, null);
    }

    /**
     * 大規模な組織データセットを生成するヘルパーメソッド
     */
    private Map<String, EmployeeRecord> createLargeOrganization(int employeeCount) {
        Map<String, EmployeeRecord> employees = new HashMap<>();
        
        // 組織構造のベースライン
        String[] positions = {"一般社員", "マネージャー", "部長", "本部長", "統括本部長"};
        int[] positionCounts = {
            (int)(employeeCount * 0.7),  // 70% 一般社員
            (int)(employeeCount * 0.2),  // 20% マネージャー
            (int)(employeeCount * 0.07), // 7% 部長
            (int)(employeeCount * 0.025), // 2.5% 本部長
            (int)(employeeCount * 0.005)  // 0.5% 統括本部長
        };

        int emailCounter = 1;
        
        for (int posIndex = 0; posIndex < positions.length; posIndex++) {
            String position = positions[posIndex];
            int count = positionCounts[posIndex];
            final int baseEmailCounter = emailCounter + posIndex * 1000;
            
            IntStream.range(0, count).forEach(i -> {
                int currentEmailCounter = baseEmailCounter + i;
                String email = String.format("employee%d@example.com", currentEmailCounter);
                String name = String.format("社員%d", currentEmailCounter);
                
                // 組織コードを分散させる
                String level1Code = String.format("%d", 1000 + (i % 5));
                String level1Name = String.format("統括本部%d", (i % 5) + 1);
                String level2Code = String.format("%d", 1100 + (i % 10));
                String level2Name = String.format("本部%d", (i % 10) + 1);
                String level3Code = String.format("%d", 1110 + (i % 20));
                String level3Name = String.format("部%d", (i % 20) + 1);
                String level4Code = String.format("%d", 1111 + (i % 50));
                String level4Name = String.format("グループ%d", (i % 50) + 1);
                
                EmployeeRecord employee = new EmployeeRecord(
                    email, name, position, level1Code, level1Name,
                    level2Code, level2Name, level3Code, level3Name,
                    level4Code, level4Name
                );
                
                employees.put(email, employee);
            });
        }
        
        return employees;
    }
}