package dev.felnull.ttsvoice.data;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.data.dictionary.DictionaryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class ConfigAndSaveDataManager {
    public static final Logger LOGGER = LogManager.getLogger(ConfigAndSaveDataManager.class);
    private static final ConfigAndSaveDataManager INSTANCE = new ConfigAndSaveDataManager();
    private static final File CONFIG_FILE = new File("./config.json5");
    private static final File SAVE_FILE = new File("./save.json");
    private static final File SERVER_SAVE_DATA_FOLDER = new File("./server_saves");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Jankson JANKSON = Jankson.builder().build();
    private final SaveData saveData = new SaveData(new File("./save.json"));
    private final Map<Long, ServerSaveData> serverSaveData = new HashMap<>();
    private final ConfigUpdater updater = new ConfigUpdater();
    private Config config;

    public static ConfigAndSaveDataManager getInstance() {
        return INSTANCE;
    }

    public boolean init(Timer timer) throws Exception {
        updater.run(CONFIG_FILE);

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

        if (saveData.load()) {
            LOGGER.info("Completed load data");
        }

        serverSaveData.putAll(loadGuildFolderSaveData(SERVER_SAVE_DATA_FOLDER, id -> new ServerSaveData(getServerSaveDataFile(id), id)));
        if (!serverSaveData.isEmpty())
            LOGGER.info("Completed load server save data");

        TimerTask saveTask = new TimerTask() {
            public void run() {
                try {
                    timerSave();
                } catch (Exception ex) {
                    LOGGER.error("Failed to periodically save data", ex);
                }
            }
        };
        timer.scheduleAtFixedRate(saveTask, 10 * 1000, 30 * 1000);
        return true;
    }

    public <T extends SaveDataBase> Map<Long, T> loadGuildFolderSaveData(File folder, Function<Long, T> createData) throws IOException {
        Map<Long, T> map = new HashMap<>();
        if (folder.exists() && folder.isDirectory()) {
            var files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".json"))
                        continue;
                    var name = file.getName();
                    name = name.substring(0, name.length() - ".json".length());
                    try {
                        long id = Long.parseLong(name);
                        var sc = createData.apply(id);
                        sc.load();
                        map.put(id, sc);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return map;
    }

    private File getServerSaveDataFile(long guildId) {
        return SERVER_SAVE_DATA_FOLDER.toPath().resolve(guildId + ".json").toFile();
    }

    private void timerSave() {
        synchronized (saveData) {
            if (System.currentTimeMillis() - Main.START_TIME >= 5 * 1000)
                saveData.setLastTime(System.currentTimeMillis());

            periodicallySaved(saveData);
        }

        synchronized (serverSaveData) {
            for (ServerSaveData value : serverSaveData.values()) {
                periodicallySaved(value);
            }
        }

        DictionaryManager.getInstance().timerSave();
    }

    public Config getConfig() {
        return config;
    }

    public ServerSaveData getServerSaveData(long guildId) {
        ServerSaveData cfg;
        synchronized (serverSaveData) {
            cfg = serverSaveData.computeIfAbsent(guildId, n -> new ServerSaveData(getServerSaveDataFile(guildId), guildId));
        }
        return cfg;
    }

    public Map<Long, ServerSaveData> getAllServerSaveData() {
        return serverSaveData;
    }

    public SaveData getSaveData() {
        return saveData;
    }

    public void periodicallySaved(final SaveDataBase saveData) {
        if (saveData.isDirty())
            saveData.doSave();
    }
}
