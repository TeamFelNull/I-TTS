package dev.felnull.itts.core.migration;

/**
 * 古いJSONデータをSQLデータベースに移行するためのサービス
 *
 * @author MORIMORI0317
 */
public interface MigrationService {

    /**
     * 移行が必要かどうかを確認
     *
     * @return 移行が必要な場合はtrue
     */
    boolean isMigrationNeeded();

    /**
     * JSONデータをSQLデータベースに移行
     *
     * @return 移行が成功した場合はtrue
     */
    boolean performMigration();

    /**
     * 移行完了後のクリーンアップ
     */
    void cleanupAfterMigration();
}