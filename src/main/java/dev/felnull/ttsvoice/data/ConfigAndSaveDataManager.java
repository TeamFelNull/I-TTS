package dev.felnull.ttsvoice.data;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigAndSaveDataManager {
    private static final Logger LOGGER = LogManager.getLogger(ConfigAndSaveDataManager.class);
    private static final ConfigAndSaveDataManager INSTANCE = new ConfigAndSaveDataManager();
    private static final File CONFIG_FILE = new File("./config.json5");
    private static final File SAVE_FILE = new File("./save.json");
    private static final File SERVER_SAVE_DATA_FOLDER = new File("./server_saves");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Jankson JANKSON = Jankson.builder().build();
    private final SaveData SAVE_DATA = new SaveData();
    private final Map<Long, ServerSaveData> SERVER_SAVE_DATA = new HashMap<>();
    private Config CONFIG;

    public static ConfigAndSaveDataManager getInstance() {
        return INSTANCE;
    }

    public void init() throws Exception {
        if (CONFIG_FILE.exists()) {
            CONFIG = Config.of(JANKSON.load(CONFIG_FILE));
            LOGGER.info("Config file was loaded");
        } else {
            LOGGER.warn("No config file, generate default config");
            CONFIG = Config.createDefault();
            try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
                CONFIG.toJson().toJson(writer, JsonGrammar.JSON5, 0);
            }
            return;
        }

        try {
            CONFIG.check();
        } catch (Exception ex) {
            LOGGER.error("Config is incorrect: " + ex.getMessage());
            return;
        }

        LOGGER.info("Completed config check");

        if (SAVE_FILE.exists()) {
            JsonObject jo;
            try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(SAVE_FILE)))) {
                jo = GSON.fromJson(reader, JsonObject.class);
            }
            SAVE_DATA.load(jo);
            LOGGER.info("Completed load data");
        }

        if (SERVER_SAVE_DATA_FOLDER.exists() && SERVER_SAVE_DATA_FOLDER.isDirectory()) {
            var files = SERVER_SAVE_DATA_FOLDER.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        var name = file.getName();
                        name = name.substring(0, name.length() - ".json".length());
                        try {
                            long id = Long.parseLong(name);
                            JsonObject jo;
                            try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)))) {
                                jo = GSON.fromJson(reader, JsonObject.class);
                            }
                            var sc = new ServerSaveData(id);
                            sc.load(jo);
                            synchronized (SERVER_SAVE_DATA) {
                                SERVER_SAVE_DATA.put(id, sc);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            LOGGER.info("Completed load server save data");
        }

        Timer timer = new Timer();
        TimerTask saveTask = new TimerTask() {
            public void run() {
                savedData();

                for (Long id : SERVER_SAVE_DATA.keySet()) {
                    savedServerData(id);
                }
            }
        };
        timer.scheduleAtFixedRate(saveTask, 0, 30 * 1000);
    }

    public Config getConfig() {
        return CONFIG;
    }

    public ServerSaveData getServerSaveData(long guildId) {
        ServerSaveData cfg;
        synchronized (SERVER_SAVE_DATA) {
            cfg = SERVER_SAVE_DATA.computeIfAbsent(guildId, n -> new ServerSaveData(guildId));
        }
        return cfg;
    }

    public Map<Long, ServerSaveData> getAllServerSaveData() {
        return SERVER_SAVE_DATA;
    }

    public SaveData getSaveData() {
        return SAVE_DATA;
    }

    public void savedData() {
        synchronized (SAVE_DATA) {
            if (SAVE_DATA.isDirty()) {
                var jo = SAVE_DATA.save();
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(SAVE_FILE)))) {
                    GSON.toJson(jo, writer);
             //       LOGGER.info("Completed to save data");
                } catch (Exception ex) {
                    LOGGER.error("Failed to save data", ex);
                }
                SAVE_DATA.setDirty(false);
            }
        }
    }

    public void savedServerData(long guildId) {
        synchronized (SERVER_SAVE_DATA) {
            var config = SERVER_SAVE_DATA.get(guildId);
            if (config != null && config.isDirty()) {
                var jo = new JsonObject();
                config.save(jo);
                if (!SERVER_SAVE_DATA_FOLDER.exists() && !SERVER_SAVE_DATA_FOLDER.mkdirs()) {
                    LOGGER.error("Failed to create server save data folder");
                }
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(SERVER_SAVE_DATA_FOLDER.toPath().resolve(guildId + ".json").toFile())))) {
                    GSON.toJson(jo, writer);
           //         LOGGER.info("Completed to server save data");
                } catch (Exception ex) {
                    LOGGER.error("Failed to server save data", ex);
                }
                config.setDirty(false);
            }
        }
    }
}
