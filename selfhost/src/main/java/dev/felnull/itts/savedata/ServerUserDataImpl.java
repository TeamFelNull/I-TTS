package dev.felnull.itts.savedata;

import com.google.gson.JsonObject;
import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * サーバーのユーザ別データの実装
 *
 * @author MORIMORI0317
 */
public class ServerUserDataImpl implements ServerUserData {

    /**
     * 音声タイプ
     */
    private final AtomicReference<String> voiceType = new AtomicReference<>(INIT_VOICE_TYPE);

    /**
     * 読み上げ拒否されいるか
     */
    private final AtomicBoolean deny = new AtomicBoolean(INIT_DENY);

    /**
     * ニックネーム
     */
    private final AtomicReference<String> nickName = new AtomicReference<>(INIT_NICK_NAME);

    /**
     * 更新時の処理
     */
    private final Runnable dirtyTo;

    /**
     * コンストラクタ
     *
     * @param dirtyTo 更新時の処理
     */
    protected ServerUserDataImpl(Runnable dirtyTo) {
        this.dirtyTo = dirtyTo;
    }

    /**
     * Jsonから読み込み
     *
     * @param jo Json
     */
    protected void loadFromJson(@NotNull JsonObject jo) {
        voiceType.set(JsonUtils.getString(jo, "voice_type", INIT_VOICE_TYPE));
        deny.set(JsonUtils.getBoolean(jo, "deny", INIT_DENY));
        nickName.set(JsonUtils.getString(jo, "nick_name", INIT_NICK_NAME));
    }

    /**
     * Jsonへ保存
     *
     * @param jo Json
     */
    protected void saveToJson(@NotNull JsonObject jo) {
        jo.addProperty("voice_type", voiceType.get());
        jo.addProperty("deny", deny.get());
        jo.addProperty("nick_name", nickName.get());
    }

    @Override
    public @Nullable String getVoiceType() {
        return this.voiceType.get();
    }

    @Override
    public void setVoiceType(@Nullable String voiceType) {
        this.voiceType.set(voiceType);
        dirtyTo.run();
    }

    @Override
    public boolean isDeny() {
        return this.deny.get();
    }

    @Override
    public void setDeny(boolean deny) {
        this.deny.set(deny);
        dirtyTo.run();
    }

    @Override
    public @Nullable String getNickName() {
        return this.nickName.get();
    }

    @Override
    public void setNickName(@Nullable String nickName) {
        this.nickName.set(nickName);
        dirtyTo.run();
    }
}
