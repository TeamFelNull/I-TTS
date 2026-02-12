package dev.felnull.itts.core.savedata.legacy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.dao.DAOFactory;
import dev.felnull.itts.core.savedata.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
