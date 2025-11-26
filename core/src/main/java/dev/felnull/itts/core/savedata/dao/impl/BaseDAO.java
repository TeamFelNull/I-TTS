package dev.felnull.itts.core.savedata.dao.impl;


import com.zaxxer.hikari.HikariDataSource;
import dev.felnull.itts.core.savedata.dao.DAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DAOのベース
 */
abstract class BaseDAO implements DAO {

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
            throw new RuntimeException("Data source creation failed", e);
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

