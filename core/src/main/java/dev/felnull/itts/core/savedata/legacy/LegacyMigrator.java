package dev.felnull.itts.core.savedata.legacy;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.repository.*;
import dev.felnull.itts.core.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 保存データ移行処理
 * 古いJson形式から、最新のDB形式へ移行する
 */
public class LegacyMigrator {

    /**
     * ロガー
     */
    private static final Logger LOGGER = LogManager.getLogger(LegacyMigrator.class);

    /**
     * Jsonが保存先ディレクトリ
     */
    private static final File JSON_SAVE_DIR = new File("./save_data");

    /**
     * グローバル辞書の保存先
     */
    private static final File GLOBAL_DICT_DIR = new File("./global_dict.json");

    /**
     * GSON
     */
    private final Gson gson = new Gson();

    private LegacyMigrator() {
    }

    private void execute(DataRepository repo) {
        migrateServerData(repo);
        migrateServerUserData(repo);
        migrateServerDictUseData(repo);
        migrateServerDictData(repo);
        migrateGlobalDictData(repo);
    }

    private void migrateServerData(DataRepository repo) {
        File dir = new File(JSON_SAVE_DIR, "server");
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        Arrays.stream(files).forEach((file) -> {
            String name = FNStringUtil.removeExtension(file.getName());
            long serverId;
            try {
                serverId = Long.parseLong(name);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid file name {}", file.getName());
                return;
            }

            JsonObject jo = loadJson(file);
            if (jo == null) {
                return;
            }

            String defaultVoiceType = JsonUtils.getString(jo, "default_voice_type", null);
            String ignoreRegex = JsonUtils.getString(jo, "ignore_regex", "(!|/|\\\\$|`).*");
            boolean needJoin = JsonUtils.getBoolean(jo, "need_join", false);
            boolean overwriteAloud = JsonUtils.getBoolean(jo, "overwrite_aloud", false);
            boolean notifyMove = JsonUtils.getBoolean(jo, "notify_move", true);
            int readLimit = JsonUtils.getInt(jo, "read_limit", 200);
            int nameReadLimit = JsonUtils.getInt(jo, "name_read_limit", 20);

            ServerData serverData = repo.getServerData(serverId);
            serverData.setDefaultVoiceType(defaultVoiceType);
            serverData.setIgnoreRegex(ignoreRegex);
            serverData.setNeedJoin(needJoin);
            serverData.setOverwriteAloud(overwriteAloud);
            serverData.setNotifyMove(notifyMove);
            serverData.setReadLimit(readLimit);
            serverData.setNameReadLimit(nameReadLimit);
        });
    }

    private void migrateServerUserData(DataRepository repo) {
        File dir = new File(JSON_SAVE_DIR, "server_users");
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        Arrays.stream(files).forEach((file) -> {
            String name = FNStringUtil.removeExtension(file.getName());
            long serverId;
            try {
                serverId = Long.parseLong(name);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid file name {}", file.getName());
                return;
            }

            JsonObject jo = loadJson(file);
            if (jo == null) {
                return;
            }

            JsonObject dataJo = jo.getAsJsonObject("data");
            if (dataJo == null) {
                return;
            }

            dataJo.entrySet().stream()
                    .filter(it -> it.getValue().isJsonObject())
                    .forEach((entry) -> {
                        long userId;
                        try {
                            userId = Long.parseLong(name);
                        } catch (NumberFormatException e) {
                            LOGGER.error("Invalid data name {}", file.getName());
                            return;
                        }
                        JsonObject entryJo = entry.getValue().getAsJsonObject();

                        String voiceType = JsonUtils.getString(entryJo, "voice_type", null);
                        boolean deny = JsonUtils.getBoolean(entryJo, "deny", false);
                        String nickName = JsonUtils.getString(entryJo, "nick_name", null);

                        ServerUserData serverUserData = repo.getServerUserData(serverId, userId);
                        serverUserData.setVoiceType(voiceType);
                        serverUserData.setDeny(deny);
                        serverUserData.setNickName(nickName);
                    });
        });
    }

    private void migrateServerDictUseData(DataRepository repo) {
        File dir = new File(JSON_SAVE_DIR, "dict_use");
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        Arrays.stream(files).forEach((file) -> {
            String name = FNStringUtil.removeExtension(file.getName());
            long serverId;
            try {
                serverId = Long.parseLong(name);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid file name {}", file.getName());
                return;
            }

            JsonObject jo = loadJson(file);
            if (jo == null) {
                return;
            }

            JsonObject dataJo = jo.getAsJsonObject("data");
            if (dataJo == null) {
                return;
            }

            dataJo.entrySet().stream()
                    .filter(it -> it.getValue().isJsonPrimitive())
                    .filter(it -> it.getValue().getAsJsonPrimitive().isNumber())
                    .forEach((entry) -> {
                        String dictName = entry.getKey();
                        int priority = entry.getValue().getAsInt();

                        DictionaryUseData dictionaryUseData = repo.getDictionaryUseData(serverId, dictName);

                        if (priority >= 0) {
                            dictionaryUseData.setEnable(true);
                            dictionaryUseData.setPriority(priority);
                        } else {
                            dictionaryUseData.setEnable(false);
                            dictionaryUseData.setPriority(null);
                        }
                    });
        });
    }

    private void migrateServerDictData(DataRepository repo) {
        File dir = new File(JSON_SAVE_DIR, "server_dict");
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        Arrays.stream(files).forEach((file) -> {
            String name = FNStringUtil.removeExtension(file.getName());
            long serverId;
            try {
                serverId = Long.parseLong(name);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid file name {}", file.getName());
                return;
            }

            JsonObject jo = loadJson(file);
            if (jo == null) {
                return;
            }

            Map<String, String> dictEntry = loadDict(jo);
            CustomDictionaryData dictionaryData = repo.getServerCustomDictionaryData(serverId);
            dictEntry.forEach((target, read) -> {
                dictionaryData.add(new CustomDictionaryEntry(target, read, ReplaceType.WORD));
            });
        });
    }

    private void migrateGlobalDictData(DataRepository repo) {
        if (!GLOBAL_DICT_DIR.exists()) {
            return;
        }

        JsonObject jo = loadJson(GLOBAL_DICT_DIR);
        if (jo == null) {
            return;
        }

        Map<String, String> dictEntry = loadDict(jo);
        CustomDictionaryData dictionaryData = repo.getGlobalCustomDictionaryData();
        dictEntry.forEach((target, read) -> dictionaryData.add(new CustomDictionaryEntry(target, read, ReplaceType.WORD)));
    }

    private Map<String, String> loadDict(JsonObject jo) {
        Map<String, String> entry = new HashMap<>();

        JsonObject dataJo = jo.getAsJsonObject("data");
        if (dataJo != null) {
            dataJo.entrySet().stream()
                    .filter(it -> it.getValue().isJsonPrimitive())
                    .filter(it -> it.getValue().getAsJsonPrimitive().isString())
                    .forEach(it -> {
                        entry.put(it.getKey(), it.getValue().getAsString());
                    });
        }

        return entry;
    }


    private JsonObject loadJson(File file) {
        JsonObject jo;

        try (Reader reader = new FileReader(file); Reader bufReader = new BufferedReader(reader)) {
            jo = gson.fromJson(bufReader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.error("Loading failed {}", file.getName(), e);
            return null;
        }

        int version = JsonUtils.getInt(jo, "version", -1);
        if (version != 0) {
            LOGGER.error("Unsupported config version {}", file.getName());
            return null;
        }

        return jo;
    }

    public static void checkAndExecution(Supplier<DAO> daoProvider) {
        // 移行が必要かどうか確認
        if (!(JSON_SAVE_DIR.exists() || GLOBAL_DICT_DIR.exists())) {
            return;
        }

        // 移行開始
        LOGGER.info("Started migration from JSON format to DB format");

        DataRepository repo = DataRepository.create(daoProvider.get());
        repo.init();
        repo.addErrorListener((error) -> LOGGER.error("DataBase Error", error));

        // Jsonを読み取ってDBに書き込む
        LegacyMigrator migrator = new LegacyMigrator();
        migrator.execute(repo);

        repo.dispose();

        // 旧データを退避
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
        String timeText = LocalDateTime.now().format(timeFormatter);
        File moveDir = new File("old_save_data-" + timeText);

        try {
            if (JSON_SAVE_DIR.exists()) {
                Files.move(JSON_SAVE_DIR, moveDir);
            }

            if (GLOBAL_DICT_DIR.exists()) {
                FNDataUtil.wishMkdir(moveDir);
                Files.move(GLOBAL_DICT_DIR, new File(moveDir, "global_dict.json"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to move Old SaveData", e);
        }

        // 移行完了
        LOGGER.info("Migration done");
    }
}
