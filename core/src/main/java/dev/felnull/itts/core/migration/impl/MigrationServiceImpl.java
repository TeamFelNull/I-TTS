package dev.felnull.itts.core.migration.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.migration.MigrationService;
import dev.felnull.itts.core.migration.data.LegacyJsonDictData;
import dev.felnull.itts.core.migration.data.LegacyJsonServerData;
import dev.felnull.itts.core.migration.data.LegacyJsonUserData;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON からSQL への移行サービスの実装
 */
public class MigrationServiceImpl implements MigrationService {

    private static final Logger LOGGER = LogManager.getLogger(MigrationServiceImpl.class);
    private static final Gson GSON = new Gson();

    // 古いJSONファイルのパス
    private final File serverDataFile;
    private final File userDataFile;
    private final File dictDataFile;
    private final File globalDictDataFile;
    private final File migrationCompletedMarker;
    private final File backupDir;

    private final SaveDataManager saveDataManager;

    public MigrationServiceImpl(SaveDataManager saveDataManager) {
        this(saveDataManager, new File("."));
    }

    public MigrationServiceImpl(SaveDataManager saveDataManager, File baseDirectory) {
        this.saveDataManager = saveDataManager;
        this.serverDataFile = new File(baseDirectory, "server_data.json");
        this.userDataFile = new File(baseDirectory, "user_data.json");
        this.dictDataFile = new File(baseDirectory, "dict_data.json");
        this.globalDictDataFile = new File(baseDirectory, "global_dict_data.json");
        this.migrationCompletedMarker = new File(baseDirectory, ".migration_completed");
        this.backupDir = new File(baseDirectory, "json_backup");
    }

    @Override
    public boolean isMigrationNeeded() {
        // 移行完了マーカーが存在する場合は移行不要
        if (migrationCompletedMarker.exists()) {
            return false;
        }

        // いずれかのJSONファイルが存在する場合は移行が必要
        return serverDataFile.exists() || 
               userDataFile.exists() || 
               dictDataFile.exists() || 
               globalDictDataFile.exists();
    }

    @Override
    public boolean performMigration() {
        LOGGER.info("Starting JSON to SQL migration...");

        try {
            // バックアップディレクトリを作成
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                LOGGER.error("Failed to create backup directory");
                return false;
            }

            boolean migrationSuccess = true;

            // サーバーデータの移行
            if (serverDataFile.exists()) {
                migrationSuccess &= migrateServerData();
            }

            // ユーザーデータの移行
            if (userDataFile.exists()) {
                migrationSuccess &= migrateUserData();
            }

            // 辞書データの移行
            if (dictDataFile.exists()) {
                migrationSuccess &= migrateDictData();
            }

            // グローバル辞書データの移行
            if (globalDictDataFile.exists()) {
                migrationSuccess &= migrateGlobalDictData();
            }

            if (migrationSuccess) {
                // 移行完了マーカーを作成
                try {
                    migrationCompletedMarker.createNewFile();
                    LOGGER.info("Migration completed successfully");
                } catch (IOException e) {
                    LOGGER.error("Failed to create migration completed marker", e);
                    return false;
                }
            }

            return migrationSuccess;

        } catch (Exception e) {
            LOGGER.error("Migration failed with exception", e);
            return false;
        }
    }

    @Override
    public void cleanupAfterMigration() {
        LOGGER.info("Performing cleanup after migration...");

        try {
            // JSONファイルをバックアップに移動
            moveFileToBackup(serverDataFile);
            moveFileToBackup(userDataFile);
            moveFileToBackup(dictDataFile);
            moveFileToBackup(globalDictDataFile);

            LOGGER.info("Cleanup completed. Original JSON files moved to backup directory");
        } catch (Exception e) {
            LOGGER.error("Error during cleanup", e);
        }
    }

    private boolean migrateServerData() {
        LOGGER.info("Migrating server data from {}", serverDataFile.getName());

        try (FileReader reader = new FileReader(serverDataFile)) {
            JsonObject rootObject = GSON.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, com.google.gson.JsonElement> entry : rootObject.entrySet()) {
                try {
                    long serverId = Long.parseLong(entry.getKey());
                    JsonObject serverDataJson = entry.getValue().getAsJsonObject();

                    LegacyJsonServerData serverData = new LegacyJsonServerData(
                            JsonUtils.getString(serverDataJson, "defaultVoiceType", null),
                            JsonUtils.getString(serverDataJson, "ignoreRegex", null),
                            JsonUtils.getBoolean(serverDataJson, "needJoin", true),
                            JsonUtils.getBoolean(serverDataJson, "overwriteAloud", true),
                            JsonUtils.getBoolean(serverDataJson, "notifyMove", false),
                            JsonUtils.getInt(serverDataJson, "readLimit", 100),
                            JsonUtils.getInt(serverDataJson, "nameReadLimit", 15)
                    );

                    // SQLに保存
                    var repository = saveDataManager.getRepository();
                    var sqlServerData = repository.getServerData(serverId);
                    
                    sqlServerData.setDefaultVoiceType(serverData.defaultVoiceType());
                    sqlServerData.setIgnoreRegex(serverData.ignoreRegex());
                    sqlServerData.setNeedJoin(serverData.needJoin());
                    sqlServerData.setOverwriteAloud(serverData.overwriteAloud());
                    sqlServerData.setNotifyMove(serverData.notifyMove());
                    sqlServerData.setReadLimit(serverData.readLimit());
                    sqlServerData.setNameReadLimit(serverData.nameReadLimit());

                    LOGGER.debug("Migrated server data for server ID: {}", serverId);

                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid server ID in JSON: {}", entry.getKey());
                } catch (Exception e) {
                    LOGGER.error("Error migrating server data for ID: {}", entry.getKey(), e);
                }
            }

            return true;

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to read server data file", e);
            return false;
        }
    }

    private boolean migrateUserData() {
        LOGGER.info("Migrating user data from {}", userDataFile.getName());

        try (FileReader reader = new FileReader(userDataFile)) {
            JsonObject rootObject = GSON.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, com.google.gson.JsonElement> serverEntry : rootObject.entrySet()) {
                try {
                    long serverId = Long.parseLong(serverEntry.getKey());
                    JsonObject usersObject = serverEntry.getValue().getAsJsonObject();

                    for (Map.Entry<String, com.google.gson.JsonElement> userEntry : usersObject.entrySet()) {
                        try {
                            long userId = Long.parseLong(userEntry.getKey());
                            JsonObject userDataJson = userEntry.getValue().getAsJsonObject();

                            LegacyJsonUserData userData = new LegacyJsonUserData(
                                    JsonUtils.getString(userDataJson, "voiceType", null),
                                    JsonUtils.getBoolean(userDataJson, "deny", false),
                                    JsonUtils.getString(userDataJson, "nickName", null)
                            );

                            // SQLに保存
                            var repository = saveDataManager.getRepository();
                            var sqlUserData = repository.getServerUserData(serverId, userId);
                            
                            sqlUserData.setVoiceType(userData.voiceType());
                            sqlUserData.setDeny(userData.deny());
                            sqlUserData.setNickName(userData.nickName());

                            LOGGER.debug("Migrated user data for server ID: {}, user ID: {}", serverId, userId);

                        } catch (NumberFormatException e) {
                            LOGGER.warn("Invalid user ID in JSON: {}", userEntry.getKey());
                        } catch (Exception e) {
                            LOGGER.error("Error migrating user data for server ID: {}, user ID: {}", serverId, userEntry.getKey(), e);
                        }
                    }

                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid server ID in user data JSON: {}", serverEntry.getKey());
                } catch (Exception e) {
                    LOGGER.error("Error migrating user data for server ID: {}", serverEntry.getKey(), e);
                }
            }

            return true;

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to read user data file", e);
            return false;
        }
    }

    private boolean migrateDictData() {
        LOGGER.info("Migrating dictionary data from {}", dictDataFile.getName());

        try (FileReader reader = new FileReader(dictDataFile)) {
            JsonObject rootObject = GSON.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, com.google.gson.JsonElement> serverEntry : rootObject.entrySet()) {
                try {
                    long serverId = Long.parseLong(serverEntry.getKey());
                    JsonObject dictObject = serverEntry.getValue().getAsJsonObject();

                    var repository = saveDataManager.getRepository();
                    var serverDictData = repository.getServerCustomDictionaryData(serverId);

                    for (Map.Entry<String, com.google.gson.JsonElement> dictEntry : dictObject.entrySet()) {
                        try {
                            String target = dictEntry.getKey();
                            String read = dictEntry.getValue().getAsString();

                            LegacyJsonDictData dictData = new LegacyJsonDictData(target, read);

                            // SQLに保存
                            CustomDictionaryEntry entry = new CustomDictionaryEntry(
                                    dictData.target(),
                                    dictData.read(),
                                    ReplaceType.WORD
                            );
                            serverDictData.add(entry);

                            LOGGER.debug("Migrated dictionary entry for server ID: {}, target: {}", serverId, target);

                        } catch (Exception e) {
                            LOGGER.error("Error migrating dictionary entry for server ID: {}, target: {}", serverId, dictEntry.getKey(), e);
                        }
                    }

                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid server ID in dictionary data JSON: {}", serverEntry.getKey());
                } catch (Exception e) {
                    LOGGER.error("Error migrating dictionary data for server ID: {}", serverEntry.getKey(), e);
                }
            }

            return true;

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to read dictionary data file", e);
            return false;
        }
    }

    private boolean migrateGlobalDictData() {
        LOGGER.info("Migrating global dictionary data from {}", globalDictDataFile.getName());

        try (FileReader reader = new FileReader(globalDictDataFile)) {
            JsonObject dictObject = GSON.fromJson(reader, JsonObject.class);

            var repository = saveDataManager.getRepository();
            var globalDictData = repository.getGlobalCustomDictionaryData();

            for (Map.Entry<String, com.google.gson.JsonElement> dictEntry : dictObject.entrySet()) {
                try {
                    String target = dictEntry.getKey();
                    String read = dictEntry.getValue().getAsString();

                    LegacyJsonDictData dictData = new LegacyJsonDictData(target, read);

                    // SQLに保存
                    CustomDictionaryEntry entry = new CustomDictionaryEntry(
                            dictData.target(),
                            dictData.read(),
                            ReplaceType.WORD
                    );
                    globalDictData.add(entry);

                    LOGGER.debug("Migrated global dictionary entry, target: {}", target);

                } catch (Exception e) {
                    LOGGER.error("Error migrating global dictionary entry, target: {}", dictEntry.getKey(), e);
                }
            }

            return true;

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to read global dictionary data file", e);
            return false;
        }
    }

    private void moveFileToBackup(@NotNull File file) {
        if (!file.exists()) {
            return;
        }

        try {
            Path source = file.toPath();
            Path backup = Paths.get(backupDir.getPath(), file.getName());
            Files.move(source, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.debug("Moved {} to backup", file.getName());
        } catch (IOException e) {
            LOGGER.error("Failed to move {} to backup", file.getName(), e);
        }
    }
}