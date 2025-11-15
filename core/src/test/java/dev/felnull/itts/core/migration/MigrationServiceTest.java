package dev.felnull.itts.core.migration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.migration.impl.MigrationServiceImpl;
import dev.felnull.itts.core.savedata.SaveDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 移行サービスのテスト
 */
class MigrationServiceTest {

    @TempDir
    Path tempDir;

    private MigrationService migrationService;
    private SaveDataManager mockSaveDataManager;
    private Gson gson;

    @BeforeEach
    void setUp() {
        mockSaveDataManager = mock(SaveDataManager.class);
        migrationService = new MigrationServiceImpl(mockSaveDataManager, tempDir.toFile());
        gson = new Gson();
    }

    @Test
    void testIsMigrationNeeded_NoJsonFiles() {
        // JSONファイルが存在しない場合
        assertFalse(migrationService.isMigrationNeeded());
    }

    @Test
    void testIsMigrationNeeded_WithServerDataFile() throws IOException {
        // server_data.json が存在する場合
        createTestServerDataFile();
        assertTrue(migrationService.isMigrationNeeded());
    }

    @Test
    void testIsMigrationNeeded_AlreadyMigrated() throws IOException {
        // 移行完了マーカーが存在する場合
        createTestServerDataFile();
        new File(tempDir.toFile(), ".migration_completed").createNewFile();
        assertFalse(migrationService.isMigrationNeeded());
    }

    @Test
    void testPerformMigration_Success() throws IOException {
        // テストデータを作成
        createTestServerDataFile();
        createTestUserDataFile();
        createTestDictDataFile();
        createTestGlobalDictDataFile();

        // 移行が必要であることを確認
        assertTrue(migrationService.isMigrationNeeded());

        // Note: 実際の移行テストはモックを使用して行う必要があります
        // ここでは基本的な構造のテストのみ実行
    }

    private void createTestServerDataFile() throws IOException {
        JsonObject serverData = new JsonObject();
        JsonObject server1 = new JsonObject();
        server1.addProperty("defaultVoiceType", "test_voice");
        server1.addProperty("ignoreRegex", "test_regex");
        server1.addProperty("needJoin", true);
        server1.addProperty("overwriteAloud", false);
        server1.addProperty("notifyMove", true);
        server1.addProperty("readLimit", 100);
        server1.addProperty("nameReadLimit", 15);
        
        serverData.add("123456789", server1);
        
        writeJsonToFile("server_data.json", serverData);
    }

    private void createTestUserDataFile() throws IOException {
        JsonObject userData = new JsonObject();
        JsonObject server1 = new JsonObject();
        JsonObject user1 = new JsonObject();
        user1.addProperty("voiceType", "user_voice");
        user1.addProperty("deny", false);
        user1.addProperty("nickName", "TestUser");
        
        server1.add("987654321", user1);
        userData.add("123456789", server1);
        
        writeJsonToFile("user_data.json", userData);
    }

    private void createTestDictDataFile() throws IOException {
        JsonObject dictData = new JsonObject();
        JsonObject server1 = new JsonObject();
        server1.addProperty("test", "てすと");
        server1.addProperty("hello", "はろー");
        
        dictData.add("123456789", server1);
        
        writeJsonToFile("dict_data.json", dictData);
    }

    private void createTestGlobalDictDataFile() throws IOException {
        JsonObject globalDict = new JsonObject();
        globalDict.addProperty("global_test", "ぐろーばるてすと");
        globalDict.addProperty("common", "こもん");
        
        writeJsonToFile("global_dict_data.json", globalDict);
    }

    private void writeJsonToFile(String fileName, JsonObject jsonObject) throws IOException {
        File file = new File(tempDir.toFile(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(jsonObject, writer);
        }
    }
}