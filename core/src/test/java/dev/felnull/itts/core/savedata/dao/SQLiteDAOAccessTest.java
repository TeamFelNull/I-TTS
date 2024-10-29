package dev.felnull.itts.core.savedata.dao;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SQLiteDAOAccessTest extends DAOAccessTest {

    @TempDir
    private static Path dbDir;

    @BeforeAll
    static void setUpAll() {
        File dbFile = new File(dbDir.toFile(), "save_data.db");
        assertFalse(dbFile.exists());

        dao = DAOFactory.getInstance().createSQLiteDAO(dbFile);
        dao.init();
    }

    @BeforeEach
    void tearDownAll() throws SQLException {
        // テストが終わるたびにデータベースをクリアする
        try (Connection connection = dao.getConnection()) {
            clearTables(connection);
        }
    }

    private void clearTables(Connection connection) throws SQLException {
        @Language("SQLite")
        String showSql = """
                select tbl_name
                from sqlite_master
                where type = 'table'
                and not tbl_name = 'sqlite_sequence';
                """;

        List<String> tables = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(showSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("tbl_name"));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("PRAGMA foreign_keys=false")) {
            statement.execute();
        }

        for (String table : tables) {
            @Language("SQLite")
            String delSql = "drop table " + table;

            try (PreparedStatement statement = connection.prepareStatement(delSql)) {
                statement.execute();
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("PRAGMA foreign_keys=true")) {
            statement.execute();
        }

        try (PreparedStatement statement = connection.prepareStatement(showSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Assertions.fail();
                }
            }
        }
    }

}
