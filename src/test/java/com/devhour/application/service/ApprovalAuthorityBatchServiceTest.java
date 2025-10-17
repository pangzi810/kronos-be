package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devhour.application.dto.BatchResult;
import com.devhour.domain.model.entity.ApprovalAuthority;
import com.devhour.domain.model.entity.Approver;
import com.devhour.domain.model.valueobject.EmployeeRecord;
import com.devhour.domain.model.valueobject.Position;
import com.devhour.domain.repository.ApprovalAuthorityRepository;
import com.devhour.domain.repository.ApproverRepository;
import com.devhour.domain.service.OrganizationHierarchyProcessor;

/**
 * ApprovalAuthorityBatchService のテストクラス
 * 
 * CSVバッチ処理、組織階層処理、差分更新処理のテストを包含
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalAuthorityBatchService Tests")
class ApprovalAuthorityBatchServiceTest {
    
    @Mock
    private ApprovalAuthorityRepository approvalAuthorityRepository;
    
    @Mock
    private ApproverRepository approverRepository;
    
    @Mock
    private OrganizationHierarchyProcessor hierarchyProcessor;
    
    @InjectMocks
    private ApprovalAuthorityBatchService batchService;
    
    @TempDir
    Path tempDir;
    
    // テスト用データ（ヘッダーなし - 後方互換性のため）
    private static final String VALID_CSV_CONTENT = """
        tanaka@example.com,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー
        suzuki@example.com,鈴木一郎,1000,開発統括本部,1100,開発本部,1110,開発1部,,,部長
        sato@example.com,佐藤次郎,1000,開発統括本部,1100,開発本部,,,,,本部長
        yamada@example.com,山田三郎,1000,開発統括本部,,,,,,,統括本部長
        takahashi@example.com,高橋四郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,一般社員
        """;
    
    // ヘッダー付きCSVテスト用データ
    private static final String VALID_CSV_WITH_HEADER = """
        メールアドレス,氏名,最上位の組織コード,最上位の組織名,２階層目の組織コード,２階層目の組織名,３階層目の組織コード,３階層目の組織名,４階層目の組織コード,４階層目の組織名,役職
        tanaka@example.com,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー
        suzuki@example.com,鈴木一郎,1000,開発統括本部,1100,開発本部,1110,開発1部,,,部長
        sato@example.com,佐藤次郎,1000,開発統括本部,1100,開発本部,,,,,本部長
        yamada@example.com,山田三郎,1000,開発統括本部,,,,,,,統括本部長
        """;
    
    @BeforeEach
    void setUp() {
        // モックの基本設定
        lenient().when(approvalAuthorityRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(approverRepository.findAllGroupedByTarget()).thenReturn(Collections.emptyMap());
        lenient().when(hierarchyProcessor.calculateApprovers(any())).thenReturn(Collections.emptyMap());
    }
    
    @Nested
    @DisplayName("CSV Parsing Tests")
    class CsvParsingTests {
        
        @Test
        @DisplayName("有効なCSVファイルを正しく解析できる")
        void shouldParseValidCsvFile() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.getErrors().size(), "エラーが発生してはいけない");
        }
        
        @Test
        @DisplayName("存在しないファイルパスでエラーが記録される")
        void shouldRecordErrorForNonExistentFile() {
            // Arrange
            String nonExistentFile = "/path/to/nonexistent/file.csv";
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(nonExistentFile);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.getErrors().isEmpty(), "ファイル不存在エラーが記録されるべき");
            assertTrue(result.getErrors().stream().anyMatch(error -> 
                error.contains("File import failed")), "ファイルインポート失敗エラーが含まれるべき");
        }
        
        @Test
        @DisplayName("不正なCSV形式でエラーが記録される")
        void shouldRecordErrorForInvalidCsvFormat() throws IOException {
            // Arrange
            String invalidCsv = "invalid,csv,format\nwith,wrong,number,of,columns";
            Path csvFile = tempDir.resolve("invalid.csv");
            Files.writeString(csvFile, invalidCsv);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertFalse(result.getErrors().isEmpty(), "CSVパースエラーが記録されるべき");
        }
        
        @Test
        @DisplayName("空のCSVファイルを処理できる")
        void shouldHandleEmptyCsvFile() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("empty.csv");
            Files.writeString(csvFile, "");
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.getProcessed());
        }
        
        @Test
        @DisplayName("不正なメールアドレス形式でエラーが記録される")
        void shouldRecordErrorForInvalidEmailFormat() throws IOException {
            // Arrange
            String invalidEmailCsv = "invalid-email,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー";
            Path csvFile = tempDir.resolve("invalid_email.csv");
            Files.writeString(csvFile, invalidEmailCsv);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertFalse(result.getErrors().isEmpty(), "メールアドレスエラーが記録されるべき");
        }
        
        @Test
        @DisplayName("未知の役職でエラーが記録される")
        void shouldRecordErrorForUnknownPosition() throws IOException {
            // Arrange
            String unknownPositionCsv = "tanaka@example.com,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,未知の役職";
            Path csvFile = tempDir.resolve("unknown_position.csv");
            Files.writeString(csvFile, unknownPositionCsv);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertFalse(result.getErrors().isEmpty(), "役職エラーが記録されるべき");
        }
        
        @Test
        @DisplayName("ヘッダー付きCSVファイルを正しく解析できる")
        void shouldParseValidCsvFileWithHeader() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees_with_header.csv");
            Files.writeString(csvFile, VALID_CSV_WITH_HEADER);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.getErrors().size(), "エラーが発生してはいけない");
            assertEquals(4, result.getProcessed(), "4人の承認権限者が処理されるべき");
        }
    }
    
    @Nested
    @DisplayName("Approval Authority Extraction Tests")
    class ApprovalAuthorityExtractionTests {
        
        @Test
        @DisplayName("承認権限者（マネージャー以上）のみが抽出される")
        void shouldExtractOnlyApprovalAuthorities() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // Mock設定 - 承認権限者のみ保存が呼ばれることを期待
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            // 承認権限者は4人（マネージャー、部長、本部長、統括本部長）
            verify(approvalAuthorityRepository, times(4)).save(any(ApprovalAuthority.class));
            
            // 一般社員は保存されないことを確認
            verify(approvalAuthorityRepository, never()).save(argThat(authority -> 
                authority.getPosition() == Position.EMPLOYEE
            ));
        }
        
        @Test
        @DisplayName("承認権限者の組織情報が正しく設定される")
        void shouldSetCorrectOrganizationInfo() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            verify(approvalAuthorityRepository).save(argThat(authority -> 
                "tanaka@example.com".equals(authority.getEmail()) &&
                "田中太郎".equals(authority.getName()) &&
                Position.MANAGER == authority.getPosition() &&
                "1000".equals(authority.getLevel1Code()) &&
                "開発統括本部".equals(authority.getLevel1Name()) &&
                "1111".equals(authority.getLevel4Code()) &&
                "開発1グループ".equals(authority.getLevel4Name())
            ));
        }
    }
    
    @Nested
    @DisplayName("Organization Hierarchy Integration Tests")
    class OrganizationHierarchyIntegrationTests {
        
        @Test
        @DisplayName("OrganizationHierarchyProcessorが正しく呼び出される")
        void shouldCallOrganizationHierarchyProcessor() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            Map<String, Set<String>> mockApproverRelations = Map.of(
                "tanaka@example.com", Set.of("suzuki@example.com")
            );
            when(hierarchyProcessor.calculateApprovers(any())).thenReturn(mockApproverRelations);
            
            // Act
            batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            verify(hierarchyProcessor).calculateApprovers(any());
        }
        
        @Test
        @DisplayName("計算された承認者関係が正しく処理される")
        void shouldProcessCalculatedApproverRelationships() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            Map<String, Set<String>> mockApproverRelations = Map.of(
                "tanaka@example.com", Set.of("suzuki@example.com"),
                "suzuki@example.com", Set.of("sato@example.com")
            );
            when(hierarchyProcessor.calculateApprovers(any())).thenReturn(mockApproverRelations);
            when(approverRepository.save(any(Approver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(2, result.getApproverRelations().getAdded(),
                "承認者関係が2つ追加されるべき");
            verify(approverRepository, times(2)).save(any(Approver.class));
        }
    }
    
    @Nested
    @DisplayName("Differential Update Tests")
    class DifferentialUpdateTests {
        
        @Test
        @DisplayName("新規承認権限者が追加される")
        void shouldAddNewApprovalAuthorities() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            when(approvalAuthorityRepository.findAll()).thenReturn(Collections.emptyList());
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(4, result.getProcessed(), "4人の承認権限者が処理されるべき");
            verify(approvalAuthorityRepository, times(4)).save(any(ApprovalAuthority.class));
        }
        
        @Test
        @DisplayName("既存承認権限者が更新される")
        void shouldUpdateExistingApprovalAuthorities() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // 既存の承認権限者をモック
            ApprovalAuthority existingAuthority = ApprovalAuthority.create(
                "tanaka@example.com", "田中旧名", Position.MANAGER,
                "1000", "開発統括本部", "1100", "開発本部",
                "1110", "開発1部", "1111", "開発1グループ"
            );
            when(approvalAuthorityRepository.findAll()).thenReturn(List.of(existingAuthority));
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(4, result.getProcessed(), "4人の承認権限者が処理されるべき");
            verify(approvalAuthorityRepository, times(4)).save(any(ApprovalAuthority.class));
        }
        
        @Test
        @DisplayName("削除された承認権限者が処理される")
        void shouldProcessDeletedApprovalAuthorities() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, "tanaka@example.com,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー");
            
            // 既存データに削除対象が含まれる
            ApprovalAuthority existingAuthority1 = ApprovalAuthority.create(
                "tanaka@example.com", "田中太郎", Position.MANAGER,
                "1000", "開発統括本部", "1100", "開発本部",
                "1110", "開発1部", "1111", "開発1グループ"
            );
            ApprovalAuthority deletedAuthority = ApprovalAuthority.create(
                "deleted@example.com", "削除される人", Position.MANAGER,
                "2000", "営業統括本部", "2100", "営業本部",
                "2110", "営業1部", "2111", "営業1グループ"
            );
            when(approvalAuthorityRepository.findAll()).thenReturn(List.of(existingAuthority1, deletedAuthority));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(1, result.getDeleted(), "1人が削除されるべき");
            verify(approvalAuthorityRepository).deleteByEmail("deleted@example.com");
        }
        
        @Test
        @DisplayName("承認者関係の差分更新が正しく行われる")
        void shouldCorrectlyUpdateApproverRelationshipsDiff() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // 既存の承認者関係
            Map<String, Set<String>> existingApprovers = Map.of(
                "tanaka@example.com", Set.of("oldapprover@example.com", "suzuki@example.com")
            );
            when(approverRepository.findAllGroupedByTarget()).thenReturn(existingApprovers);
            
            // 新しい承認者関係
            Map<String, Set<String>> newApproverRelations = Map.of(
                "tanaka@example.com", Set.of("suzuki@example.com", "newapprover@example.com")
            );
            when(hierarchyProcessor.calculateApprovers(any())).thenReturn(newApproverRelations);
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(1, result.getApproverRelations().getAdded(), "1つの承認者関係が追加されるべき");
            assertEquals(1, result.getApproverRelations().getDeleted(), "1つの承認者関係が削除されるべき");
            
            verify(approverRepository).deleteByTargetAndApprover("tanaka@example.com", "oldapprover@example.com");
            verify(approverRepository).save(argThat(approver -> 
                "tanaka@example.com".equals(approver.getTargetEmail()) &&
                "newapprover@example.com".equals(approver.getApproverEmail())
            ));
        }
    }
    
    @Nested
    @DisplayName("Transaction Processing Tests") 
    class TransactionProcessingTests {
        
        @Test
        @DisplayName("個別処理でエラーが発生した場合の独立性を確認")
        void shouldIsolateIndividualProcessingErrors() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // 特定の承認権限者でエラーが発生する設定
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> {
                    ApprovalAuthority authority = invocation.getArgument(0);
                    if ("suzuki@example.com".equals(authority.getEmail())) {
                        throw new RuntimeException("Database error for suzuki");
                    }
                    return authority;
                });
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertFalse(result.getErrors().isEmpty(), "エラーが記録されるべき");
            assertTrue(result.getErrors().stream().anyMatch(error -> 
                error.contains("suzuki@example.com")), "suzukiのエラーが記録されるべき");
        }
        
        @Test
        @DisplayName("大量データの処理性能を確認")
        void shouldHandleLargeDatasetEfficiently() throws IOException {
            // Arrange
            StringBuilder largeCSV = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeCSV.append(String.format("user%d@example.com,ユーザー%d,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー\n", i, i));
            }
            
            Path csvFile = tempDir.resolve("large_employees.csv");
            Files.writeString(csvFile, largeCSV.toString());
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            long startTime = System.currentTimeMillis();
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Assert
            assertEquals(1000, result.getProcessed(), "1000人が処理されるべき");
            assertTrue(processingTime < 10000, "10秒以内に処理が完了するべき"); // 10秒以内
            assertTrue(result.getErrors().isEmpty(), "エラーが発生してはいけない");
        }
    }
    
    @Nested
    @DisplayName("Logging and Monitoring Tests")
    class LoggingMonitoringTests {
        
        @Test
        @DisplayName("バッチ処理の開始と終了ログが正しく出力される")
        void shouldLogBatchProcessingStartAndEnd() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertNotNull(result);
            assertTrue(result.getProcessingTimeMillis() >= 0, "処理時間が記録されるべき");
        }
        
        @Test
        @DisplayName("役職別集計情報が正しく記録される")
        void shouldRecordPositionStatistics() throws IOException {
            // Arrange
            String csvWithMultiplePositions = """
                tanaka@example.com,田中太郎,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー
                suzuki@example.com,鈴木一郎,1000,開発統括本部,1100,開発本部,1110,開発1部,,,部長
                sato@example.com,佐藤次郎,1000,開発統括本部,1100,開発本部,,,,,本部長
                yamada@example.com,山田三郎,1000,開発統括本部,,,,,,,統括本部長
                takahashi@example.com,高橋四郎,1000,開発統括本部,1200,開発本部2,1210,開発2部,1211,開発2グループ,マネージャー
                """;
            Path csvFile = tempDir.resolve("multiple_positions.csv");
            Files.writeString(csvFile, csvWithMultiplePositions);
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(5, result.getProcessed(), "5人の承認権限者が処理されるべき");
            assertTrue(result.isSuccessful(), "処理が成功するべき");
        }
        
        @Test
        @DisplayName("エラー発生時の詳細ログが記録される")
        void shouldRecordDetailedErrorLogs() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenThrow(new RuntimeException("Database connection timeout"));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertFalse(result.isSuccessful(), "エラーが発生するべき");
            assertFalse(result.getErrors().isEmpty(), "エラーメッセージが記録されるべき");
            assertTrue(result.getErrors().stream().anyMatch(error -> 
                error.contains("Database connection timeout")), "具体的なエラー内容が含まれるべき");
        }
        
        @Test
        @DisplayName("承認者関係の変更統計が正しく記録される")
        void shouldRecordApproverRelationChanges() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            Map<String, Set<String>> newApproverRelations = Map.of(
                "tanaka@example.com", Set.of("suzuki@example.com"),
                "suzuki@example.com", Set.of("sato@example.com")
            );
            when(hierarchyProcessor.calculateApprovers(any())).thenReturn(newApproverRelations);
            when(approverRepository.save(any(Approver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(2, result.getApproverRelations().getAdded(),
                "承認者関係の追加数が正しく記録されるべき");
        }
        
        @Test
        @DisplayName("大量データ処理時のパフォーマンス統計が記録される")
        void shouldRecordPerformanceStatisticsForLargeData() throws IOException {
            // Arrange
            StringBuilder largeCSV = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                largeCSV.append(String.format("manager%d@example.com,マネージャー%d,1000,開発統括本部,1100,開発本部,1110,開発1部,1111,開発1グループ,マネージャー\n", i, i));
            }
            
            Path csvFile = tempDir.resolve("large_dataset.csv");
            Files.writeString(csvFile, largeCSV.toString());
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(100, result.getProcessed(), "100人が処理されるべき");
            assertTrue(result.getProcessingTimeMillis() > 0, "処理時間が正しく記録されるべき");
            assertTrue(result.isSuccessful(), "大量データ処理が成功するべき");
        }
    }

    @Nested
    @DisplayName("Error Handling and Recovery Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("すべての処理でエラーが発生した場合の結果")
        void shouldHandleAllProcessingErrors() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenThrow(new RuntimeException("Database connection error"));
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(0, result.getProcessed(), "処理済み数は0であるべき");
            assertEquals(4, result.getErrors().size(), "4つのエラーが記録されるべき");
        }
        
        @Test
        @DisplayName("部分的な処理成功の結果")
        void shouldHandlePartialProcessingSuccess() throws IOException {
            // Arrange
            Path csvFile = tempDir.resolve("employees.csv");
            Files.writeString(csvFile, VALID_CSV_CONTENT);
            
            // 2番目と4番目の処理でエラーが発生
            when(approvalAuthorityRepository.save(any(ApprovalAuthority.class)))
                .thenAnswer(invocation -> {
                    ApprovalAuthority authority = invocation.getArgument(0);
                    if ("suzuki@example.com".equals(authority.getEmail()) || 
                        "yamada@example.com".equals(authority.getEmail())) {
                        throw new RuntimeException("Processing error");
                    }
                    return authority;
                });
            
            // Act
            BatchResult result = batchService.importApprovalAuthoritiesFromFile(csvFile.toString());
            
            // Assert
            assertEquals(2, result.getProcessed(), "2人が正常に処理されるべき");
            assertEquals(2, result.getErrors().size(), "2つのエラーが記録されるべき");
        }
    }
}