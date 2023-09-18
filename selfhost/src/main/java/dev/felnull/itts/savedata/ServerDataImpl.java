package dev.felnull.itts.savedata;

import com.google.gson.JsonObject;
import dev.felnull.itts.core.savedata.ServerData;
import dev.felnull.itts.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * サーバーデータの実装
 *
 * @author MORIMORI0317
 */
public class ServerDataImpl extends SaveDataBase implements ServerData {

    /**
     * デフォルトの音声タイプ
     */
    private final AtomicReference<String> defaultVoiceType = new AtomicReference<>(INIT_DEFAULT_VOICE_TYPE);

    /**
     * 無視する正規表現
     */
    private final AtomicReference<String> ignoreRegex = new AtomicReference<>(INIT_IGNORE_REGEX);

    /**
     * 参加しているときのみ読み上げ
     */
    private final AtomicBoolean needJoin = new AtomicBoolean(INIT_NEED_JOIN);

    /**
     * 読み上げを上書き
     */
    private final AtomicBoolean overwriteAloud = new AtomicBoolean(INIT_OVERWRITE_ALOUD);

    /**
     * 移動時に読み上げるか
     */
    private final AtomicBoolean notifyMove = new AtomicBoolean(INIT_NOTIFY_MOVE);

    /**
     * 最大読み上げ数
     */
    private final AtomicInteger readLimit = new AtomicInteger(INIT_READ_LIMIT);

    /**
     * 最大名前読み上げ数
     */
    private final AtomicInteger nameReadLimit = new AtomicInteger(INIT_NAME_READ_LIMIT);

    @Override
    public String getName() {
        return "Server Data";
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        defaultVoiceType.set(JsonUtils.getString(jo, "default_voice_type", INIT_DEFAULT_VOICE_TYPE));
        ignoreRegex.set(JsonUtils.getString(jo, "ignore_regex", INIT_IGNORE_REGEX));
        needJoin.set(JsonUtils.getBoolean(jo, "need_join", INIT_NEED_JOIN));
        overwriteAloud.set(JsonUtils.getBoolean(jo, "overwrite_aloud", INIT_OVERWRITE_ALOUD));
        notifyMove.set(JsonUtils.getBoolean(jo, "notify_move", INIT_NOTIFY_MOVE));
        readLimit.set(JsonUtils.getInt(jo, "read_limit", INIT_READ_LIMIT));
        nameReadLimit.set(JsonUtils.getInt(jo, "name_read_limit", INIT_NAME_READ_LIMIT));
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        jo.addProperty("default_voice_type", defaultVoiceType.get());
        jo.addProperty("ignore_regex", ignoreRegex.get());
        jo.addProperty("need_join", needJoin.get());
        jo.addProperty("overwrite_aloud", overwriteAloud.get());
        jo.addProperty("notify_move", notifyMove.get());
        jo.addProperty("read_limit", readLimit.get());
        jo.addProperty("name_read_limit", nameReadLimit.get());
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }


    @Override
    public @Nullable String getDefaultVoiceType() {
        return this.defaultVoiceType.get();
    }

    @Override
    public void setDefaultVoiceType(@Nullable String voiceType) {
        this.defaultVoiceType.set(voiceType);
        dirty();
    }

    @Override
    public @Nullable String getIgnoreRegex() {
        return this.ignoreRegex.get();
    }

    @Override
    public void setIgnoreRegex(@Nullable String ignoreRegex) {
        this.ignoreRegex.set(ignoreRegex);
        dirty();
    }

    @Override
    public boolean isNeedJoin() {
        return this.needJoin.get();
    }

    @Override
    public void setNeedJoin(boolean needJoin) {
        this.needJoin.set(needJoin);
        dirty();
    }

    @Override
    public boolean isOverwriteAloud() {
        return this.overwriteAloud.get();
    }

    @Override
    public void setOverwriteAloud(boolean overwriteAloud) {
        this.overwriteAloud.set(overwriteAloud);
        dirty();
    }

    @Override
    public boolean isNotifyMove() {
        return this.notifyMove.get();
    }

    @Override
    public void setNotifyMove(boolean notifyMove) {
        this.notifyMove.set(notifyMove);
        dirty();
    }

    @Override
    public int getReadLimit() {
        return this.readLimit.get();
    }

    @Override
    public void setReadLimit(int readLimit) {
        this.readLimit.set(readLimit);
        dirty();
    }

    @Override
    public int getNameReadLimit() {
        return this.nameReadLimit.get();
    }

    @Override
    public void setNameReadLimit(int nameReadLimit) {
        this.nameReadLimit.set(nameReadLimit);
        dirty();
    }
}
