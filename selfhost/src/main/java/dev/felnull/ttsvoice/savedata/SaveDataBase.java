package dev.felnull.ttsvoice.savedata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.Main;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public abstract class SaveDataBase {
    private static final Gson GSON = new Gson();
    private static final long SAVE_WAIT = 3000;
    private final Object dirtyLock = new Object();
    private final Object saveLock = new Object();
    private final AtomicLong dirtyTime = new AtomicLong(-1);
    private final File saveFile;

    protected SaveDataBase(File saveFile) {
        this.saveFile = saveFile;
    }

    public void loadExistingData() {
        if (this.saveFile.exists()) {
            try (Reader reader = new FileReader(this.saveFile); Reader bufReader = new BufferedReader(reader)) {
                loadFromJson(GSON.fromJson(bufReader, JsonObject.class));
                Main.RUNTIME.getLogger().debug("Succeeded in loading the existing save data. ({})", getName());
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed in loading the existing save data. ({})", getName(), ex);
            }
        }
    }

    public abstract String getName();

    protected abstract void loadFromJson(@NotNull JsonObject jo);

    protected abstract void saveToJson(@NotNull JsonObject jo);

    protected void setDirty() {
        synchronized (dirtyLock) {
            boolean dirtied = this.dirtyTime.compareAndSet(-1, System.currentTimeMillis());
            if (dirtied) {
                saveWaitStart();
            } else {
                this.dirtyTime.set(System.currentTimeMillis());
            }
        }
    }

    private void saveWaitStart() {
        CompletableFuture.runAsync(this::saveWait, Main.RUNTIME.getAsyncWorkerExecutor()).thenAcceptAsync(v -> save(), Main.RUNTIME.getAsyncWorkerExecutor());
    }

    private void saveWait() {
        long wait = SAVE_WAIT;
        do {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            wait = SAVE_WAIT - System.currentTimeMillis() - this.dirtyTime.get();
        } while (wait > 0);
        this.dirtyTime.set(-1);
    }

    private void save() {
        synchronized (saveLock) {
            FNDataUtil.wishMkdir(saveFile.getParentFile());
            try (Writer writer = new FileWriter(saveFile); Writer bufWriter = new BufferedWriter(writer)) {
                var jo = new JsonObject();
                saveToJson(jo);
                GSON.toJson(jo, bufWriter);
                Main.RUNTIME.getLogger().debug("Succeeded to save saved data. ({})", getName());
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to save saved data. ({})", getName(), ex);
            }
        }
    }
}
