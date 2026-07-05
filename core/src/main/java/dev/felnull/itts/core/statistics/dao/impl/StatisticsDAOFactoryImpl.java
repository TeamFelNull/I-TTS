package dev.felnull.itts.core.statistics.dao.impl;

import dev.felnull.itts.core.statistics.dao.StatisticsDAO;
import dev.felnull.itts.core.statistics.dao.StatisticsDAOFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 統計DAOファクトリの実装クラス
 */
public final class StatisticsDAOFactoryImpl implements StatisticsDAOFactory {

    /**
     * インスタンス
     */
    public static final StatisticsDAOFactoryImpl INSTANCE = new StatisticsDAOFactoryImpl();

    @Override
    public StatisticsDAO createSQLiteDAO(@NotNull File dbFile) {
        return new SQLiteStatisticsDAO(dbFile);
    }

    @Override
    public StatisticsDAO createMysqlDAO(@NotNull String host, int port, @NotNull String databaseName, @NotNull String user, @NotNull String password) {
        return new MySQLStatisticsDAO(host, port, databaseName, user, password);
    }
}
