package dev.felnull.itts.core.savedata.dao.impl;

import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.DAOFactory;

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
    public DAO createSQliteDAO(File dbFile) {
        return new SQLiteDAO(dbFile);
    }

    @Override
    public DAO createMysqlDAO(String host, int port, String databaseName, String user, String password) {
        return null;
    }

}
