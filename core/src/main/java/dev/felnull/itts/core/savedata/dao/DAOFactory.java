package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.savedata.dao.impl.DAOFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * DAO作成クラス
 */
public interface DAOFactory {

    static DAOFactory getInstance() {
        return DAOFactoryImpl.INSTANCE;
    }

    /**
     * SQLiteのDAOを作成
     *
     * @param dbFile データベースのファイル
     * @return DAO
     */
    DAO createSQLiteDAO(File dbFile);

    /**
     * MySQLのDAOを作成
     *
     * @param host         ホスト名
     * @param port         ポート番号
     * @param databaseName データベース名
     * @param user         ユーザー名
     * @param password     パスワード
     * @return DAO
     */
    DAO createMysqlDAO(@NotNull String host, int port, @NotNull String databaseName, @NotNull String user, @NotNull String password);
}

