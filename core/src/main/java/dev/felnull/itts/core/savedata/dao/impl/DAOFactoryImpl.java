package dev.felnull.itts.core.savedata.dao.impl;

import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * DAOファクトリの実装クラス
 */
public final class DAOFactoryImpl implements DAOFactory {

    /**
     * インスタンス
     */
    public static final DAOFactoryImpl INSTANCE = new DAOFactoryImpl();

    @Override
    public DAO createSQLiteDAO(File dbFile) {
        return new SQLiteDAO(dbFile);
    }

    @Override
    public DAO createMysqlDAO(@NotNull String host, int port, @NotNull String databaseName, @NotNull String user, @NotNull String password) {
        return new MySQLDAO(host, port, databaseName, user, password);
    }
}
