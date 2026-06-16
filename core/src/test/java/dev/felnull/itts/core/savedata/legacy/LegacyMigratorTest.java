package dev.felnull.itts.core.savedata.legacy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import dev.felnull.itts.core.savedata.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LegacyMigratorTest {

    private static final long SERVER_ID = 1219628077353668688L;
    private static final long USER_ID_1 = 1346500316383805532L;
    private static final long USER_ID_2 = 328520268274204673L;

    private final Gson gson = new Gson();

    private DataRepository createRepository(Path tempDir) {
        File dbFile = new File(tempDir.toFile(), "test.db");
        DataRepository repo = DataRepository.create(
                DAOFactory.getInstance().createSQLiteDAO(dbFile));
        repo.init();
        return repo;
    }

    private void writeJson(File file, JsonObject jo) throws IOException {
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(jo, writer);
        }
    }

    private void writeRawFile(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private JsonObject createVersionedJson() {
        JsonObject jo = new JsonObject();
        jo.addProperty("version", 0);
        return jo;
    }

    @Test
    void testMigrateServerData(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject serverJson = createVersionedJson();
        serverJson.addProperty("ignore_regex", "(!|/|\\\\$|`).*");
        serverJson.addProperty("need_join", true);
        serverJson.addProperty("overwrite_aloud", true);
        serverJson.addProperty("notify_move", false);
        serverJson.addProperty("read_limit", 100);
        serverJson.addProperty("name_read_limit", 10);

        writeJson(new File(saveDir, "server/" + SERVER_ID + ".json"), serverJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.execute(repo);

        ServerData serverData = repo.getServerData(SERVER_ID);
        assertNull(serverData.getDefaultVoiceType());
        assertEquals("(!|/|\\\\$|`).*", serverData.getIgnoreRegex());
        assertTrue(serverData.isNeedJoin());
        assertTrue(serverData.isOverwriteAloud());
        assertFalse(serverData.isNotifyMove());
        assertEquals(100, serverData.getReadLimit());
        assertEquals(10, serverData.getNameReadLimit());

        repo.dispose();
    }

    @Test
    void testMigrateServerUserData_userIdNotServerId(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject user1 = new JsonObject();
        user1.addProperty("deny", true);
        user1.addProperty("nick_name", "テストユーザー1");

        JsonObject user2 = new JsonObject();
        user2.addProperty("deny", false);
        user2.addProperty("voice_type", "katyou");

        JsonObject data = new JsonObject();
        data.add(String.valueOf(USER_ID_1), user1);
        data.add(String.valueOf(USER_ID_2), user2);

        JsonObject usersJson = createVersionedJson();
        usersJson.add("data", data);

        writeJson(new File(saveDir, "server_users/" + SERVER_ID + ".json"), usersJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.execute(repo);

        ServerUserData userData1 = repo.getServerUserData(SERVER_ID, USER_ID_1);
        assertTrue(userData1.isDeny());
        assertEquals("テストユーザー1", userData1.getNickName());
        assertNull(userData1.getVoiceType());

        ServerUserData userData2 = repo.getServerUserData(SERVER_ID, USER_ID_2);
        assertFalse(userData2.isDeny());
        assertEquals("katyou", userData2.getVoiceType());
        assertNull(userData2.getNickName());

        repo.dispose();
    }

    @Test
    void testMigrateServerDictData(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject data = new JsonObject();
        data.addProperty("test", "テスト");
        data.addProperty("hello", "こんにちは");

        JsonObject dictJson = createVersionedJson();
        dictJson.add("data", data);

        writeJson(new File(saveDir, "server_dict/" + SERVER_ID + ".json"), dictJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.execute(repo);

        CustomDictionaryData dictData = repo.getServerCustomDictionaryData(SERVER_ID);
        List<IdCustomDictionaryEntryPair> entries = dictData.getAll();
        assertEquals(2, entries.size());

        Set<String> targets = entries.stream()
                .map(e -> e.entry().target())
                .collect(Collectors.toSet());
        assertTrue(targets.contains("test"));
        assertTrue(targets.contains("hello"));

        repo.dispose();
    }

    @Test
    void testMigrateGlobalDictData(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject data = new JsonObject();
        data.addProperty("world", "せかい");
        data.addProperty("abc", "えーびーしー");

        JsonObject dictJson = createVersionedJson();
        dictJson.add("data", data);

        writeJson(globalDict, dictJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.execute(repo);

        CustomDictionaryData dictData = repo.getGlobalCustomDictionaryData();
        List<IdCustomDictionaryEntryPair> entries = dictData.getAll();
        assertEquals(2, entries.size());

        Set<String> targets = entries.stream()
                .map(e -> e.entry().target())
                .collect(Collectors.toSet());
        assertTrue(targets.contains("world"));
        assertTrue(targets.contains("abc"));

        entries.forEach(e -> assertEquals(ReplaceType.WORD, e.entry().replaceType()));

        repo.dispose();
    }

    @Test
    void testMigrateServerDictUseData(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject data = new JsonObject();
        data.addProperty("server", 2);
        data.addProperty("global", 3);
        data.addProperty("unit", -1);

        JsonObject dictUseJson = createVersionedJson();
        dictUseJson.add("data", data);

        writeJson(new File(saveDir, "dict_use/" + SERVER_ID + ".json"), dictUseJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.execute(repo);

        DictionaryUseData serverDict = repo.getDictionaryUseData(SERVER_ID, "server");
        assertEquals(true, serverDict.isEnable());
        assertEquals(2, serverDict.getPriority());

        DictionaryUseData globalDictUse = repo.getDictionaryUseData(SERVER_ID, "global");
        assertEquals(true, globalDictUse.isEnable());
        assertEquals(3, globalDictUse.getPriority());

        DictionaryUseData unitDict = repo.getDictionaryUseData(SERVER_ID, "unit");
        assertEquals(false, unitDict.isEnable());
        assertNull(unitDict.getPriority());

        repo.dispose();
    }

    @Test
    void testMalformedJsonSkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        writeRawFile(new File(saveDir, "server/" + SERVER_ID + ".json"), "{invalid json!!!");

        JsonObject validUsersJson = createVersionedJson();
        JsonObject userData = new JsonObject();
        JsonObject user = new JsonObject();
        user.addProperty("deny", true);
        userData.add(String.valueOf(USER_ID_1), user);
        validUsersJson.add("data", userData);
        writeJson(new File(saveDir, "server_users/" + SERVER_ID + ".json"), validUsersJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        ServerUserData serverUserData = repo.getServerUserData(SERVER_ID, USER_ID_1);
        assertTrue(serverUserData.isDeny());

        repo.dispose();
    }

    @Test
    void testEmptyJsonFileSkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        writeRawFile(new File(saveDir, "server/" + SERVER_ID + ".json"), "");

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        repo.dispose();
    }

    @Test
    void testDictMigrationIdempotent(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject serverDictData = new JsonObject();
        serverDictData.addProperty("aaa", "えーえーえー");
        serverDictData.addProperty("bbb", "びーびーびー");

        JsonObject serverDictJson = createVersionedJson();
        serverDictJson.add("data", serverDictData);
        writeJson(new File(saveDir, "server_dict/" + SERVER_ID + ".json"), serverDictJson);

        JsonObject globalDictData = new JsonObject();
        globalDictData.addProperty("xxx", "えっくすえっくすえっくす");

        JsonObject globalDictJson = createVersionedJson();
        globalDictJson.add("data", globalDictData);
        writeJson(globalDict, globalDictJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        migrator.execute(repo);
        migrator.execute(repo);

        CustomDictionaryData serverDict = repo.getServerCustomDictionaryData(SERVER_ID);
        assertEquals(2, serverDict.getAll().size());

        CustomDictionaryData globalDictionary = repo.getGlobalCustomDictionaryData();
        assertEquals(1, globalDictionary.getAll().size());

        repo.dispose();
    }

    @Test
    void testNonExistentDirectoriesSkipped(@TempDir Path tempDir) {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        repo.dispose();
    }

    @Test
    void testInvalidFileNameSkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject validJson = createVersionedJson();
        validJson.add("data", new JsonObject());

        writeJson(new File(saveDir, "server/not_a_number.json"), validJson);
        writeJson(new File(saveDir, "server_users/invalid.json"), validJson);
        writeJson(new File(saveDir, "dict_use/abc.json"), validJson);
        writeJson(new File(saveDir, "server_dict/xyz.json"), validJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        repo.dispose();
    }

    @Test
    void testInvalidUserIdSkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject invalidUser = new JsonObject();
        invalidUser.addProperty("deny", true);

        JsonObject validUser = new JsonObject();
        validUser.addProperty("deny", true);

        JsonObject data = new JsonObject();
        data.add("not_a_number", invalidUser);
        data.add(String.valueOf(USER_ID_1), validUser);

        JsonObject usersJson = createVersionedJson();
        usersJson.add("data", data);

        writeJson(new File(saveDir, "server_users/" + SERVER_ID + ".json"), usersJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        ServerUserData userData = repo.getServerUserData(SERVER_ID, USER_ID_1);
        assertTrue(userData.isDeny());

        repo.dispose();
    }

    @Test
    void testUnsupportedVersionSkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject serverJson = new JsonObject();
        serverJson.addProperty("version", 1);
        serverJson.addProperty("need_join", true);

        writeJson(new File(saveDir, "server/" + SERVER_ID + ".json"), serverJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        ServerData serverData = repo.getServerData(SERVER_ID);
        assertFalse(serverData.isNeedJoin());

        repo.dispose();
    }

    @Test
    void testMissingDataKeySkipped(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject noDataJson = createVersionedJson();

        writeJson(new File(saveDir, "server_users/" + SERVER_ID + ".json"), noDataJson);
        writeJson(new File(saveDir, "dict_use/" + SERVER_ID + ".json"), noDataJson);
        writeJson(new File(saveDir, "server_dict/" + SERVER_ID + ".json"), noDataJson);
        writeJson(globalDict, noDataJson);

        DataRepository repo = createRepository(tempDir);
        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);

        assertDoesNotThrow(() -> migrator.execute(repo));

        CustomDictionaryData serverDict = repo.getServerCustomDictionaryData(SERVER_ID);
        assertTrue(serverDict.getAll().isEmpty());

        CustomDictionaryData globalDictData = repo.getGlobalCustomDictionaryData();
        assertTrue(globalDictData.getAll().isEmpty());

        repo.dispose();
    }

    @Test
    void testMoveOldData_bothExist(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");
        File moveDir = new File(tempDir.toFile(), "old_save_data");

        new File(saveDir, "server").mkdirs();
        writeRawFile(new File(saveDir, "server/123.json"), "{}");
        writeRawFile(globalDict, "{}");

        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.moveOldData(moveDir);

        assertTrue(moveDir.exists());
        assertTrue(new File(moveDir, "save_data").exists());
        assertTrue(new File(moveDir, "save_data/server/123.json").exists());
        assertTrue(new File(moveDir, "global_dict.json").exists());

        assertFalse(saveDir.exists());
        assertFalse(globalDict.exists());
    }

    @Test
    void testMoveOldData_onlySaveDir(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");
        File moveDir = new File(tempDir.toFile(), "old_save_data");

        saveDir.mkdirs();
        writeRawFile(new File(saveDir, "server/123.json"), "{}");

        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.moveOldData(moveDir);

        assertTrue(new File(moveDir, "save_data").exists());
        assertFalse(new File(moveDir, "global_dict.json").exists());
        assertFalse(saveDir.exists());
    }

    @Test
    void testMoveOldData_onlyGlobalDict(@TempDir Path tempDir) throws IOException {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");
        File moveDir = new File(tempDir.toFile(), "old_save_data");

        writeRawFile(globalDict, "{}");

        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        migrator.moveOldData(moveDir);

        assertTrue(moveDir.exists());
        assertFalse(new File(moveDir, "save_data").exists());
        assertTrue(new File(moveDir, "global_dict.json").exists());
        assertFalse(globalDict.exists());
    }

    @Test
    void testFileErrorIsolation(@TempDir Path tempDir) throws Exception {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        long serverId1 = 111111111111111111L;
        long serverId2 = 222222222222222222L;

        JsonObject server1 = createVersionedJson();
        server1.addProperty("need_join", true);
        writeJson(new File(saveDir, "server/" + serverId1 + ".json"), server1);

        JsonObject server2 = createVersionedJson();
        server2.addProperty("need_join", true);
        writeJson(new File(saveDir, "server/" + serverId2 + ".json"), server2);

        File dbFile = new File(tempDir.toFile(), "test.db");
        DAO dao = DAOFactory.getInstance().createSQLiteDAO(dbFile);
        DAO spyDao = Mockito.spy(dao);
        DAO.ServerDataTable spyTable = Mockito.spy(spyDao.serverDataTable());
        Mockito.doThrow(new IllegalStateException("test"))
                .doCallRealMethod()
                .when(spyTable)
                .insertRecordIfNotExists(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(spyDao.serverDataTable()).thenReturn(spyTable);

        DataRepository repo = DataRepository.create(spyDao);
        repo.init();

        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        assertDoesNotThrow(() -> migrator.execute(repo));

        boolean s1 = repo.getServerData(serverId1).isNeedJoin();
        boolean s2 = repo.getServerData(serverId2).isNeedJoin();
        assertTrue(s1 || s2, "At least one server should have been migrated");

        repo.dispose();
    }

    @Test
    void testPhaseErrorIsolation(@TempDir Path tempDir) throws Exception {
        File saveDir = new File(tempDir.toFile(), "save_data");
        File globalDict = new File(tempDir.toFile(), "global_dict.json");

        JsonObject serverJson = createVersionedJson();
        serverJson.addProperty("need_join", true);
        writeJson(new File(saveDir, "server/" + SERVER_ID + ".json"), serverJson);

        JsonObject user = new JsonObject();
        user.addProperty("deny", true);
        JsonObject data = new JsonObject();
        data.add(String.valueOf(USER_ID_1), user);
        JsonObject usersJson = createVersionedJson();
        usersJson.add("data", data);
        writeJson(new File(saveDir, "server_users/" + SERVER_ID + ".json"), usersJson);

        File dbFile = new File(tempDir.toFile(), "test.db");
        DAO dao = DAOFactory.getInstance().createSQLiteDAO(dbFile);
        DAO spyDao = Mockito.spy(dao);
        DAO.ServerDataTable spyTable = Mockito.spy(spyDao.serverDataTable());
        Mockito.doThrow(new IllegalStateException("test"))
                .when(spyTable)
                .insertRecordIfNotExists(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(spyDao.serverDataTable()).thenReturn(spyTable);

        DataRepository repo = DataRepository.create(spyDao);
        repo.init();

        LegacyMigrator migrator = new LegacyMigrator(saveDir, globalDict);
        assertDoesNotThrow(() -> migrator.execute(repo));

        ServerUserData userData = repo.getServerUserData(SERVER_ID, USER_ID_1);
        assertTrue(userData.isDeny());

        repo.dispose();
    }
}
