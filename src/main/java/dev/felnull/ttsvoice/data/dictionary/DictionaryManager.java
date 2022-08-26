package dev.felnull.ttsvoice.data.dictionary;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.felnull.ttsvoice.data.ConfigAndSaveDataManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger(DictionaryManager.class);
    private static final DictionaryManager INSTANCE = new DictionaryManager();
    private static final File GLOBAL_SAVE_DICTIONARY_FILE = new File("./dictionary.json");
    private static final File SERVER_SAVE_DICTIONARY_FOLDER = new File("./server_dictionaries");
    private final SavedDictionary globalSavedDictionary = new SavedDictionary(new File("./dictionary.json"), true);
    private final List<Dictionary> globalDictionaries = ImmutableList.of(globalSavedDictionary);
    private final Map<Long, SavedDictionary> guildDictionaries = new HashMap<>();

    public static DictionaryManager getInstance() {
        return INSTANCE;
    }

    public void init() throws IOException {
        if (globalSavedDictionary.load()) {
            LOGGER.info("Completed load global dictionary");
        } else {
            globalSavedDictionary.setDirty(true);
            globalSavedDictionary.doSave();
        }

        /*
        FNDataUtil.watchFile(GLOBAL_SAVE_DICTIONARY_FILE.toPath(), (watchEvent, path) -> {
            try {
                synchronized (globalSavedDictionary) {
                    globalSavedDictionary.load(GLOBAL_SAVE_DICTIONARY_FILE);
                }
                LOGGER.info("Update global dictionary");
            } catch (Exception e) {
                LOGGER.error("Failed to load global dictionary", e);
            }
        }, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
*/
        guildDictionaries.putAll(ConfigAndSaveDataManager.getInstance().loadGuildFolderSaveData(SERVER_SAVE_DICTIONARY_FOLDER, id -> new SavedDictionary(getGuildDictFile(id), false)));
        if (!guildDictionaries.isEmpty())
            LOGGER.info("Completed load server dictionary");
    }

    private File getGuildDictFile(long guildId) {
        return SERVER_SAVE_DICTIONARY_FOLDER.toPath().resolve(guildId + ".json").toFile();
    }

    public void timerSave() {
        var cas = ConfigAndSaveDataManager.getInstance();
        synchronized (globalSavedDictionary) {
            cas.periodicallySaved(globalSavedDictionary);
        }

        synchronized (guildDictionaries) {
            for (SavedDictionary dictionary : guildDictionaries.values()) {
                cas.periodicallySaved(dictionary);
            }
        }
    }

    public List<Dictionary> getGlobalDictionaries() {
        synchronized (globalDictionaries) {
            return ImmutableList.copyOf(globalDictionaries);
        }
    }

    public SavedDictionary getGuildDictionary(long guildId) {
        synchronized (guildDictionaries) {
            return guildDictionaries.computeIfAbsent(guildId, i -> new SavedDictionary(getGuildDictFile(guildId), false));
        }
    }

    public String replace(long guildId, String text) {
        synchronized (globalDictionaries) {
            for (Dictionary dict : globalDictionaries) {
                text = dict.replace(text);
            }
        }

        text = getGuildDictionary(guildId).replace(text);
        return text;
    }
}
