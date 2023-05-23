package dev.felnull.itts.savedata;

import com.google.gson.JsonObject;
import dev.felnull.itts.core.savedata.ServerUserData;
import dev.felnull.itts.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerUserDataImpl implements ServerUserData {
    private final AtomicReference<String> voiceType = new AtomicReference<>(INIT_VOICE_TYPE);
    private final AtomicBoolean deny = new AtomicBoolean(INIT_DENY);
    private final AtomicReference<String> nickName = new AtomicReference<>(INIT_NICK_NAME);
    private final Runnable dirtyTo;

    protected ServerUserDataImpl(Runnable dirtyTo) {
        this.dirtyTo = dirtyTo;
    }

    protected void loadFromJson(@NotNull JsonObject jo) {
        voiceType.set(JsonUtils.getString(jo, "voice_type", INIT_VOICE_TYPE));
        deny.set(JsonUtils.getBoolean(jo, "deny", INIT_DENY));
        nickName.set(JsonUtils.getString(jo, "nick_name", INIT_NICK_NAME));
    }

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
