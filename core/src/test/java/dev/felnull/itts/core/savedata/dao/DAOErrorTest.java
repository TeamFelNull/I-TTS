package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.savedata.MySQLTestOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class DAOErrorTest {

    @TempDir
    private static Path dbDir;

    @Test
    void testSQLiteFileExists() throws Exception {
        File dbFile = new File(dbDir.toFile(), "save_data.db");
        assertFalse(dbFile.exists());

        Files.writeString(dbFile.toPath(), """
                お兄さん許してDBファイルに上書きされるなんて嫌よ!嫌よ！嫌よ
                おデータ壊れる！
                """, StandardCharsets.UTF_8);

        assertTrue(dbFile.exists());
        assertEquals(1, Objects.requireNonNull(dbDir.toFile().list()).length);

        byte[] data = Files.readAllBytes(dbFile.toPath());

        // DBファイル以外が存在する場合はエラーを吐くか確認
        DAO dao = DAOFactory.getInstance().createSQLiteDAO(dbFile);
        assertThrowsExactly(RuntimeException.class, dao::init);
        dao.dispose();

        // 元のデータが壊れていないか確認
        assertArrayEquals(data, Files.readAllBytes(dbFile.toPath()));
        assertEquals(1, Objects.requireNonNull(dbDir.toFile().list()).length);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "I_TTS_MYSQL_TEST", matches = "ENABLE")
    void testMySQLConnectionFailure() {
        DAO dao = DAOFactory.getInstance().createMysqlDAO(MySQLTestOperation.HOST, MySQLTestOperation.PORT + 1, MySQLTestOperation.DATABASE_NAME, MySQLTestOperation.USER, MySQLTestOperation.PASSWORD);
        assertThrowsExactly(RuntimeException.class, dao::init);
        dao.dispose();
    }

}
