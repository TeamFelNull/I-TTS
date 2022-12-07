package dev.felnull.ttsvoice.savedata;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.core.savedata.ServerUserData;
import dev.felnull.ttsvoice.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServerUserDataImpl extends SaveDataBase implements ServerUserData {
    private final long guildId;
    private final long userId;
    private final AtomicReference<String> voiceType = new AtomicReference<>(INIT_VOICE_TYPE);
    private final AtomicBoolean deny = new AtomicBoolean(INIT_DENY);
    private final AtomicReference<String> nickName = new AtomicReference<>(INIT_NICK_NAME);

    protected ServerUserDataImpl(long guildId, long userId) {
        super(new File(SelfHostSaveDataManager.getServerUserDataFolder(guildId), userId + ".json"));
        this.guildId = guildId;
        this.userId = userId;
    }

    @Override
    public String getName() {
        return "Server User Data: " + guildId + "-" + userId;
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        voiceType.set(JsonUtils.getString(jo, "voice_type", INIT_VOICE_TYPE));
        deny.set(JsonUtils.getBoolean(jo, "deny", INIT_DENY));
        nickName.set(JsonUtils.getString(jo, "nick_name", INIT_NICK_NAME));
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        jo.addProperty("voice_type", voiceType.get());
        jo.addProperty("deny", deny.get());
        jo.addProperty("nick_name", nickName.get());
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    public @Nullable String getVoiceType() {
        return this.voiceType.get();
    }

    @Override
    public void setVoiceType(@Nullable String voiceType) {
        this.voiceType.set(voiceType);
        dirty();
    }

    @Override
    public boolean isDeny() {
        return this.deny.get();
    }

    @Override
    public void setDeny(boolean deny) {
        this.deny.set(deny);
        dirty();
    }

    @Override
    public @Nullable String getNickName() {
        return this.nickName.get();
    }

    @Override
    public void setNickName(@NotNull String nickName) {
        this.nickName.set(nickName);
        dirty();
    }
}
