package dev.felnull.itts.savedata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.Main;
import dev.felnull.itts.core.util.JsonUtils;
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
    private boolean canSave;

    protected SaveDataBase(File saveFile) {
        this.saveFile = saveFile;
    }

    public void init() throws Exception {
        if (this.saveFile.exists()) {
            try (Reader reader = new FileReader(this.saveFile); Reader bufReader = new BufferedReader(reader)) {
                var jo = GSON.fromJson(bufReader, JsonObject.class);
                int version = JsonUtils.getInt(jo, "version", -1);

                if (version != getVersion())
                    throw new RuntimeException("Unsupported config version.");

                loadFromJson(jo);
                Main.RUNTIME.getLogger().debug("Succeeded in loading the existing save data. ({})", getName());
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed in loading the existing save data. ({})", getName(), ex);
            }
        } else {
            //saveOnly();
            setDefault();
        }

        canSave = true;
    }

    public void setDefault() {
    }

    public abstract String getName();

    protected abstract void loadFromJson(@NotNull JsonObject jo);

    protected abstract void saveToJson(@NotNull JsonObject jo);

    protected abstract int getVersion();

    protected void dirty() {
        if (!canSave)
            return;

        synchronized (dirtyLock) {
            if (this.dirtyTime.compareAndSet(-1, System.currentTimeMillis())) {
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
            try {
                saveOnly();
                Main.RUNTIME.getLogger().debug("Succeeded to save saved data. ({})", getName());
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to save saved data. ({})", getName(), ex);
            }
        }
    }

    private void saveOnly() throws Exception {
        FNDataUtil.wishMkdir(saveFile.getParentFile());
        try (Writer writer = new FileWriter(saveFile); Writer bufWriter = new BufferedWriter(writer)) {
            var jo = new JsonObject();
            saveToJson(jo);
            jo.addProperty("version", getVersion());
            GSON.toJson(jo, bufWriter);
        }
    }
}