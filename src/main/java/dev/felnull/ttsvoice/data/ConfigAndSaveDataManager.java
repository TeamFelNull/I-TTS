package dev.felnull.ttsvoice.data;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.Main;
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
    private final SaveData saveData = new SaveData();
    private final Map<Long, ServerSaveData> serverSaveData = new HashMap<>();
    private Config config;

    public static ConfigAndSaveDataManager getInstance() {
        return INSTANCE;
    }

    public boolean init() throws Exception {
        if (CONFIG_FILE.exists()) {
            config = Config.of(JANKSON.load(CONFIG_FILE));
            LOGGER.info("Config file was loaded");
        } else {
            LOGGER.warn("No config file, generate default config");
            config = Config.createDefault();
            try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
                config.toJson().toJson(writer, JsonGrammar.JSON5, 0);
            }
            return false;
        }

        try {
            config.check();
        } catch (Exception ex) {
            LOGGER.error("Config is incorrect: " + ex.getMessage());
            return false;
        }

        LOGGER.info("Completed config check");

        if (SAVE_FILE.exists()) {
            JsonObject jo;
            try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(SAVE_FILE)))) {
                jo = GSON.fromJson(reader, JsonObject.class);
            }
            saveData.load(jo);
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
                            synchronized (serverSaveData) {
                                serverSaveData.put(id, sc);
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

                for (Long id : serverSaveData.keySet()) {
                    savedServerData(id);
                }
            }
        };
        timer.scheduleAtFixedRate(saveTask, 10 * 1000, 30 * 1000);
        return true;
    }

    public Config getConfig() {
        return config;
    }

    public ServerSaveData getServerSaveData(long guildId) {
        ServerSaveData cfg;
        synchronized (serverSaveData) {
            cfg = serverSaveData.computeIfAbsent(guildId, n -> new ServerSaveData(guildId));
        }
        return cfg;
    }

    public Map<Long, ServerSaveData> getAllServerSaveData() {
        return serverSaveData;
    }

    public SaveData getSaveData() {
        return saveData;
    }

    public void savedData() {
        synchronized (saveData) {
            if (System.currentTimeMillis() - Main.START_TIME >= 5 * 1000)
                saveData.setLastTime(System.currentTimeMillis());

            if (saveData.isDirty()) {
                var jo = saveData.save();
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(SAVE_FILE)))) {
                    GSON.toJson(jo, writer);
                    //       LOGGER.info("Completed to save data");
                } catch (Exception ex) {
                    LOGGER.error("Failed to save data", ex);
                }
                saveData.setDirty(false);
            }
        }
    }

    public void savedServerData(long guildId) {
        synchronized (serverSaveData) {
            var config = serverSaveData.get(guildId);
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
