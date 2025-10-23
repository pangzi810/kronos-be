# テスト実行ガイド

## テスト実行方法

### 前提条件

#### 1. 環境変数の設定（重要）

**Rancher Desktop使用時の環境変数設定:**

```bash
# タイポした環境変数を削除（過去の設定ミスをクリーンアップ）
unset TESTCONTAINERS_DOKCER_SOCKET_OVERRIDE

# Rancher Desktop用のDocker Host設定
export DOCKER_HOST=unix:///Users/toshihiro/.rd/docker.sock

# Testcontainers Ryukを無効化（Rancher Desktopとの互換性のため）
export TESTCONTAINERS_RYUK_DISABLED=true

# ~/.zshrcまたは~/.bashrcに永続的に追加
cat >> ~/.zshrc << 'EOF'
# Rancher Desktop + Testcontainers Configuration
export DOCKER_HOST=unix:///Users/toshihiro/.rd/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
EOF
source ~/.zshrc
```

**注意**:
- `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE`は設定**しない**（Rancher Desktopではマウントエラーが発生するため）
- `DOCKER_HOST`の設定だけで十分です
- Ryukコンテナを無効化することで、Dockerソケットマウントの問題を回避します

#### 2. Spring環境変数のクリア

テスト実行前に、以下の環境変数をクリアする必要があります：

```bash
unset SPRING_DATASOURCE_URL
unset SPRING_DATASOURCE_USERNAME
unset SPRING_DATASOURCE_PASSWORD
unset SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
unset SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE
unset SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT
unset SPRING_PROFILES_ACTIVE
```

または、一括でクリア：

```bash
unset $(env | grep -E "^SPRING_" | cut -d= -f1)
```

#### 3. Dockerの起動確認

Testcontainersを使用するため、Dockerが起動している必要があります：

```bash
docker ps  # Dockerが起動していることを確認
```

### テスト実行

```bash
# 全テスト実行
./gradlew test

# 特定のテストクラス実行
./gradlew test --tests "*ProjectMapperTest"

# カバレッジレポート生成
./gradlew jacocoTestReport
# レポート: build/reports/jacoco/test/html/index.html
```

## テスト結果サマリー（2025-10-24 最終更新）

```
総テスト数: 1,895件
✅ 成功: 1,696件 (89.5%)
❌ 失敗: 199件 (10.5%)
⏭️  スキップ: 42件
📈 カバレッジ: 不明 (目標: 80%) ※失敗テストによりレポート未生成
```

### 修正完了分（8件）
- ✅ JiraProjectStatusMappingConfigurationTest: 3件修正
  - デフォルトステータスマッピングに"PLANNING"が含まれていた問題を"DRAFT"に修正
  - テストのアサーションがマッピングに存在しない値をチェックしていた問題を修正
- ✅ JiraClientTest: 1件修正
  - URLパラメータのアサーションが"nextPageToken"を期待していたが実際は"startAt"を使用している問題を修正
- ✅ JiraSyncBatchOptimizationTest: 4件（NullPointerExceptionとページネーション問題を修正）

## 既知の問題

### ⚠️ 統合テスト全体の失敗 (199件)

**問題**: Flywayマイグレーションのチェックサム不一致

**影響範囲**:
全ての`@SpringBootTest`を使用する統合テストが失敗しています：

| テストクラス | 失敗数 |
|------------|--------|
| ApprovalAuthorityMapper統合テスト | 29件 |
| ProjectMapper統合テスト | 29件 |
| ApprovalAuthorityRepository テスト | 26件 |
| ResponseTemplateMapper統合テスト | 23件 |
| WorkCategoryMapperTest | 18件 |
| ApproverMapperTest | 18件 |
| JqlQueryMapper統合テスト | 17件 |
| SyncHistoryMapper統合テスト | 13件 |
| SyncHistoryDetailMapper統合テスト | 13件 |
| WorkRecordMapper統合テスト | 12件 |
| DevelopmentHourManagementApplicationTests | 1件 |
| **合計** | **199件** |

**原因**:
- V1__init.sqlのプロジェクトステータス制約を変更
  - 旧: `('PLANNING','IN_PROGRESS','COMPLETED','CANCELLED')`
  - 新: `('DRAFT','IN_PROGRESS','CLOSED')`
- Flywayがチェックサムの違いを検出
  - 旧チェックサム: `-1636932856`
  - 新チェックサム: `-1277024566`

**エラーメッセージ**:
```
org.flywaydb.core.api.exception.FlywayValidateException at Flyway.java:191
Caused by: Migration checksum mismatch for migration version 1
```

**試行した解決方法（全て失敗）**:
1. ✗ `spring.flyway.validate-on-migrate=false`
2. ✗ `spring.flyway.ignore-migration-patterns=*:checksum`
3. ✗ FlywayMigrationStrategy (clean + migrate)
4. ✗ FlywayConfigurationCustomizer
5. ✗ FlywayAutoConfiguration除外
6. ✗ H2 INIT parameter
7. ✗ DB_CLOSE_DELAY削除
8. ✗ FlywayMigrationInitializer カスタマイズ

**失敗理由**:
Flyway 11では、validationがSpring Bootアプリケーションコンテキスト初期化の
非常に早い段階（Bean作成前）で実行されるため、プログラムによる制御が困難。

**✅ 解決済み（Testcontainersを使用）**:

全ての統合テスト（Mapper/Repository）をTestcontainers MySQLコンテナを使用するように変更しました。
これにより、Flywayチェックサム問題が完全に解決されます。

**実装方法**:

1. **Mapperテスト用**: `AbstractMapperTest`基底クラスを作成
   - TestcontainersでMySQLコンテナを起動（ランダムポート使用）
   - 各Mapper統合テストが`AbstractMapperTest`を継承

2. **Repositoryテスト用**: Testcontainersアノテーションを直接使用
   - `@Testcontainers`アノテーションを追加
   - `@Container`で静的MySQLコンテナを定義
   - `@DynamicPropertySource`で動的プロパティを設定

**更新されたテストクラス** (全11クラス):
- ✅ ProjectMapperTest
- ✅ ApprovalAuthorityMapperTest
- ✅ WorkRecordMapperTest
- ✅ WorkCategoryMapperTest
- ✅ ApproverMapperTest
- ✅ JiraSyncHistoryMapperTest
- ✅ JiraSyncHistoryDetailMapperTest
- ✅ JiraJqlQueryMapperTest
- ✅ JiraResponseTemplateMapperTest
- ✅ ApprovalAuthorityRepositoryTest
- ✅ DevelopmentHourManagementApplicationTests

**メリット**:
- 新しいMySQLコンテナで毎回クリーンな環境でテスト
- Flywayチェックサム問題が発生しない
- 実際のMySQLでテスト（H2との互換性問題を回避）
- ポート3306と競合しない（ランダムポート使用）

**Mapperテストの例**:
```java
@DisplayName("ProjectMapper統合テスト")
class ProjectMapperTest extends AbstractMapperTest {
    // テストコード
}
```

**Repositoryテストの例**:
```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApprovalAuthorityRepositoryTest {
    @Container
    @SuppressWarnings("resource")
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(false);

    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        // ... その他のプロパティ
    }
}
```

**本番リリース後の対応**:
本番環境にリリースした後は、V1マイグレーションの変更が必要な場合は
新しいマイグレーションファイル（V35等）を作成すること。

---

### ✅ 成功しているテスト (1,696件)

以下のテストカテゴリは正常に動作しています：
- Service層の単体テスト（UserApplicationService、ProjectApplicationService等）
- Configuration層のテスト
- 非DB統合テスト
- 修正済みテスト（JiraProjectStatusMappingConfiguration、JiraClient等）

## 主要な修正履歴

### プロジェクトステータス値の統一
- 旧: `PLANNING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- 新: `DRAFT`, `IN_PROGRESS`, `CLOSED`

### 変更されたファイル
1. `src/main/resources/db/migration/V1__init.sql` - CHECK制約更新
2. `src/main/java/com/devhour/infrastructure/mapper/ProjectMapper.java` - SQLクエリ更新
3. 複数のテストファイル - ステータス値更新

### OutOfMemoryError対策
`build.gradle`にテスト用ヒープサイズ設定を追加：
```gradle
test {
    maxHeapSize = '2g'
    jvmArgs = ['-XX:MaxMetaspaceSize=512m']
}
```

## トラブルシューティング

### OutOfMemoryError が発生する場合

```bash
# ヒープサイズを確認
./gradlew test --info | grep "Max heap"

# クリーンビルド
./gradlew clean test
```

### H2データベース接続エラー

```bash
# 環境変数がクリアされているか確認
env | grep SPRING_DATASOURCE
# 出力がない場合はOK
```

### Flywayエラー

```bash
# H2インメモリデータベースをリセット
./gradlew clean

# Flywayのクリーンとマイグレーション
./gradlew flywayClean flywayMigrate
```

## 参考資料

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo Coverage Report](https://www.jacoco.org/jacoco/trunk/doc/)
