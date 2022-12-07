package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ServerUserData {
    int VERSION = 0;
    String INIT_VOICE_TYPE = null;
    boolean INIT_DENY = false;
    String INIT_NICK_NAME = null;

    @Nullable
    String getVoiceType();

    void setVoiceType(@Nullable String voiceType);

    boolean isDeny();

    void setDeny(boolean deny);

    @Nullable
    String getNickName();

    void setNickName(@NotNull String nickName);
}
