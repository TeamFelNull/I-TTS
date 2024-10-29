package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.savedata.MySQLTestOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.SQLException;

@EnabledIfEnvironmentVariable(named = "I_TTS_MYSQL_TEST", matches = "ENABLE")
public class MySQLDAOAccessTest extends DAOAccessTest {

    @BeforeAll
    static void setUpAll() {
        dao = MySQLTestOperation.createDAO();
        dao.init();
    }

    @BeforeEach
    void tearDownAll() throws SQLException {
        // テストが終わるたびにデータベースをクリアする
        try (Connection connection = dao.getConnection()) {
            MySQLTestOperation.clearDataBase(connection);
        }
    }
}
