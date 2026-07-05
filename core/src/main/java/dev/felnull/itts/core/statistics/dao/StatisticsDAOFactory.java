package dev.felnull.itts.core.statistics.dao;

import dev.felnull.itts.core.statistics.dao.impl.StatisticsDAOFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 統計DAO作成クラス
 */
public interface StatisticsDAOFactory {

    /**
     * インスタンスを取得
     *
     * @return ファクトリインスタンス
     */
    static StatisticsDAOFactory getInstance() {
        return StatisticsDAOFactoryImpl.INSTANCE;
    }

    /**
     * SQLiteの統計DAOを作成
     *
     * @param dbFile データベースのファイル
     * @return DAO
     */
    StatisticsDAO createSQLiteDAO(@NotNull File dbFile);

    /**
     * MySQLの統計DAOを作成
     *
     * @param host         ホスト名
     * @param port         ポート番号
     * @param databaseName データベース名
     * @param user         ユーザー名
     * @param password     パスワード
     * @return DAO
     */
    StatisticsDAO createMysqlDAO(@NotNull String host, int port, @NotNull String databaseName, @NotNull String user, @NotNull String password);
}
