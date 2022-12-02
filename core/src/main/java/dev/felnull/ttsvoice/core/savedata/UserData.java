package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserData {
    int VERSION = 0;
    String DEFAULT_VOICE_TYPE = null;
    boolean DEFAULT_DENY = false;
    String DEFAULT_NICK_NAME = null;

    @Nullable
    String getVoiceType();

    void setVoiceType(@Nullable String voiceType);

    boolean isDeny();

    void setDeny(boolean deny);

    @Nullable
    String getNickName();

    void setNickName(@NotNull String nickName);
}
