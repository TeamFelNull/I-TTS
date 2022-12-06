package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.Nullable;

public interface ServerData {
    int VERSION = 0;
    String INIT_DEFAULT_VOICE_TYPE = null;
    String INIT_IGNORE_REGEX = "(!|/|\\\\$|`).*";
    boolean INIT_NEED_JOIN = false;
    boolean INIT_OVERWRITE_ALOUD = true;
    boolean INIT_NOTIFY_MOVE = true;
    int INIT_READ_LIMIT = 200;
    int INIT_NAME_READ_LIMIT = 10;

    @Nullable
    String getDefaultVoiceType();

    void setDefaultVoiceType(@Nullable String voiceType);

    @Nullable
    String getIgnoreRegex();

    void setIgnoreRegex(@Nullable String ignoreRegex);

    boolean isNeedJoin();

    void setNeedJoin(boolean needJoin);

    boolean isOverwriteAloud();

    void setOverwriteAloud(boolean overwriteAloud);

    boolean isNotifyMove();

    void setNotifyMove(boolean notifyMove);

    int getReadLimit();

    void setReadLimit(int readLimit);

    int getNameReadLimit();

    void setNameReadLimit(int nameReadLimit);
}
