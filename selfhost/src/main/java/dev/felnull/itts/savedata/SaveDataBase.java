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

/**
 * セーブデータのベース
 *
 * @author MORIMORI0317
 */
public abstract class SaveDataBase {

    /**
     * GSON
     */
    private static final Gson GSON = new Gson();

    /**
     * セーブデータの保存までの期間
     */
    private static final long SAVE_WAIT = 3000;

    /**
     * 保存すべきかののロック
     */
    private final Object dirtyLock = new Object();

    /**
     * 保存のロック
     */
    private final Object saveLock = new Object();

    /**
     * 更新した時の時間
     */
    private final AtomicLong dirtyTime = new AtomicLong(-1);

    /**
     * 保存先ファイル
     */
    private File saveFile;

    /**
     * セーブデータのキー
     */
    private SaveDataKey saveDataKey;

    /**
     * 保存可能かどうか
     */
    private boolean canSave;

    /**
     * コンストラクタ
     *
     * @param saveFile    保存先ファイル
     * @param saveDataKey セーブデータのキー
     * @throws Exception 例外
     */
    public void init(File saveFile, SaveDataKey saveDataKey) throws Exception {
        this.saveFile = saveFile;
        this.saveDataKey = saveDataKey;

        if (this.saveFile.exists()) {
            try (Reader reader = new FileReader(this.saveFile); Reader bufReader = new BufferedReader(reader)) {
                JsonObject jo = GSON.fromJson(bufReader, JsonObject.class);
                int version = JsonUtils.getInt(jo, "version", -1);

                if (version != getVersion()) {
                    throw new RuntimeException("Unsupported config version.");
                }

                loadFromJson(jo);
            }
        } else {
            setDefault();
        }

        canSave = true;
    }

    /**
     * 破棄
     *
     * @throws Exception 例外
     */
    public void dispose() throws Exception {
        saveOnly();
    }

    /**
     * デフォルトの状態指定
     */
    public void setDefault() {
    }

    /**
     * 名前を取得
     *
     * @return 名前
     */
    public abstract String getName();

    /**
     * Jsonから読み込む
     *
     * @param jo Json
     */
    protected abstract void loadFromJson(@NotNull JsonObject jo);

    /**
     * Jsonに保存
     *
     * @param jo Json
     */
    protected abstract void saveToJson(@NotNull JsonObject jo);

    /**
     * バージョンを取得
     *
     * @return バージョン
     */
    protected abstract int getVersion();

    /**
     * 保存すべきかを更新
     */
    protected void dirty() {
        if (!canSave) {
            return;
        }

        synchronized (dirtyLock) {
            if (this.dirtyTime.compareAndSet(-1, System.currentTimeMillis())) {
                saveWaitStart();
            } else {
                this.dirtyTime.set(System.currentTimeMillis());
            }
        }
    }

    private void saveWaitStart() {
        CompletableFuture.runAsync(this::saveWait, Main.runtime.getAsyncWorkerExecutor())
                .thenAcceptAsync(v -> save(), Main.runtime.getAsyncWorkerExecutor());
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
            String name = getName();
            if (saveDataKey != null) {
                name += ": " + saveDataKey;
            }

            try {
                saveOnly();
                Main.runtime.getLogger().debug("Succeeded to save saved data. ({})", name);
            } catch (Exception ex) {
                Main.runtime.getLogger().error("Failed to save saved data. ({})", name, ex);
            }
        }
    }

    private void saveOnly() throws Exception {
        FNDataUtil.wishMkdir(saveFile.getParentFile());
        try (Writer writer = new FileWriter(saveFile); Writer bufWriter = new BufferedWriter(writer)) {
            JsonObject jo = new JsonObject();
            saveToJson(jo);
            jo.addProperty("version", getVersion());
            GSON.toJson(jo, bufWriter);
        }
    }
}
