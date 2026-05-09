package dev.felnull.itts.core.statistics.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 統計DAOのベース
 */
abstract class BaseStatisticsDAO implements StatisticsDAO {

    /**
     * データソース
     */
    private HikariDataSource dataSource;

    /**
     * データソースを作成
     *
     * @return データソース
     */
    protected abstract HikariDataSource createDataSource();

    @Override
    public void init() {
        try {
            this.dataSource = createDataSource();
        } catch (RuntimeException e) {
            throw new RuntimeException("Statistics data source creation failed", e);
        }
    }

    @Override
    public void dispose() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
