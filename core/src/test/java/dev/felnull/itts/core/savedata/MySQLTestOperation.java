package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MySQLTestOperation {
    public static final String HOST = "localhost";
    public static final int PORT = 3306;
    public static final String DATABASE_NAME = "I_TTS_TEST";
    public static final String USER = "root";
    public static final String PASSWORD = "nktidksg110";

    public static DAO createDAO() {
        return DAOFactory.getInstance().createMysqlDAO(MySQLTestOperation.HOST, MySQLTestOperation.PORT, MySQLTestOperation.DATABASE_NAME, MySQLTestOperation.USER, MySQLTestOperation.PASSWORD);
    }

    public static void clearDataBase(Connection connection) throws SQLException {
        @Language("MySQL")
        String showSql = "show tables";

        List<String> tables = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(showSql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        }

        for (String table : tables) {
            @Language("MySQL")
            String delSql1 = "SET FOREIGN_KEY_CHECKS=0;";

            @Language("MySQL")
            String delSql2 = "truncate table " + MySQLTestOperation.DATABASE_NAME + "." + table + ";";

            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(delSql1)) {
                statement.execute();
            }

            try (PreparedStatement statement = connection.prepareStatement(delSql2)) {
                statement.execute();
            }

            connection.commit();
            connection.setAutoCommit(true);
        }

        for (String table : tables) {
            @Language("MySQL")
            String delSql = "drop table " + table;

            try (PreparedStatement statement = connection.prepareStatement(delSql)) {
                statement.execute();
            }
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
