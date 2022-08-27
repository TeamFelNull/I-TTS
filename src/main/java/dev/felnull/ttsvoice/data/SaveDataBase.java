package dev.felnull.ttsvoice.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.io.watcher.FileSystemWatcher;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.Main;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class SaveDataBase {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Function<String, DataUpdateWatcher> WATCHERS = FNDataUtil.memoize(path -> {
        var duw = new DataUpdateWatcher();
        try {
            FNDataUtil.wishMkdir(Paths.get(path).toFile());
            FNDataUtil.watchDirectory(Paths.get(path), duw, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return duw;
    });
    private final File saveFile;
    private WaitTimeThread dirtyWaitTimeThread;
    private WaitTimeThread fileWatcherWaitTimeThread;
    private boolean dirty;
    private long lastSaveWrite = -1;

    protected SaveDataBase(File saveFile) {
        this.saveFile = saveFile;
        WATCHERS.apply(saveFile.getParentFile().toString()).add(this);
    }

    abstract public JsonObject save();

    abstract public void load(JsonObject jo);

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean load() throws IOException {
        if (saveFile.exists()) {
            JsonObject jo;
            try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(saveFile)))) {
                jo = GSON.fromJson(reader, JsonObject.class);
            }
            load(jo);
            return true;
        }
        return false;
    }

    public void doSave() {
        try {
            lastSaveWrite = System.currentTimeMillis();
            FNDataUtil.wishMkdir(saveFile.getParentFile());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(saveFile)))) {
                GSON.toJson(save(), writer);
            } catch (Exception ex) {
                ConfigAndSaveDataManager.LOGGER.error("Failed to save data", ex);
            }
        } finally {
            dirty = false;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void saved() {
        dirty = true;
        if (dirtyWaitTimeThread == null) {
            dirtyWaitTimeThread = new WaitTimeThread(() -> {
                try {
                    doSave();
                } finally {
                    dirtyWaitTimeThread = null;
                }
            });
            dirtyWaitTimeThread.setName("save-wait-thread");
            dirtyWaitTimeThread.start();
        } else {
            dirtyWaitTimeThread.update();
        }
    }

    private void onFileUpdate(WatchEvent<Path> watchEvent, Path path) {
        if (lastSaveWrite != -1 && System.currentTimeMillis() - lastSaveWrite <= 1000 * 5) return;

        if (fileWatcherWaitTimeThread == null) {
            fileWatcherWaitTimeThread = new WaitTimeThread(() -> {
                try {
                    if (load()) {
                        Main.updateAllGuildCommand();
                        ConfigAndSaveDataManager.LOGGER.info("Loaded by file watcher: " + path);
                    }
                } catch (Exception ex) {
                    ConfigAndSaveDataManager.LOGGER.info("Load failed in file watcher: " + path, ex);
                } finally {
                    fileWatcherWaitTimeThread = null;
                }
            });
            fileWatcherWaitTimeThread.setName("file-watcher-wait-thread");
            fileWatcherWaitTimeThread.start();
        } else {
            fileWatcherWaitTimeThread.update();
        }
    }

    private static class DataUpdateWatcher implements FileSystemWatcher.WatchEventListener {
        private final List<SaveDataBase> saveDataBases = new ArrayList<>();

        private void add(SaveDataBase saveDataBase) {
            synchronized (saveDataBases) {
                saveDataBases.add(saveDataBase);
            }
        }

        @Override
        public void onWatchEvent(@NotNull WatchEvent<Path> watchEvent, @NotNull Path path) {
            synchronized (saveDataBases) {
                for (SaveDataBase saveDataBase : saveDataBases) {
                    if (saveDataBase.saveFile.getName().equals(path.toFile().getName()))
                        saveDataBase.onFileUpdate(watchEvent, path);
                }
            }
        }
    }
}
