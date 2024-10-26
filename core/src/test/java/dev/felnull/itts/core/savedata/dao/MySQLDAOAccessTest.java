package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.savedata.MySQLTestOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDAOAccessTest extends DAOAccessTest {

    @BeforeAll
    static void setupAll() {
        dao = MySQLTestOperation.createDAO();
        dao.init();
    }

    @BeforeEach
    void clearDataBase() throws SQLException {
        // テストが終わるたびにデータベースをクリアする
        try (Connection connection = dao.getConnection()) {
            MySQLTestOperation.clearDataBase(connection);
        }
    }
}
