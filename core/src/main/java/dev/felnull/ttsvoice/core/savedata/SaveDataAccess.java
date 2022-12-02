package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface SaveDataAccess {
    boolean init();

    @NotNull
    ServerData getServerData(long guildId);

    @NotNull
    UserData getServerUserData(long guildId, long userId);

    @NotNull
    @Unmodifiable
    List<DictUseData> getAllDictUseData(long guildId);

    @Nullable
    DictUseData getDictUseData(long guildId, @NotNull String dictId);

    void addDictUseData(long guildId, @NotNull String dictId, int priority);

    void removeDictUseData(long guildId, @NotNull String dictId);

    @NotNull
    BotServerData getBotServerData(long botUserId, long guildId);

    @NotNull
    @Unmodifiable
    List<DictData> getAllDictData(long guildId);

    @Nullable
    DictData getDictData(long guildId, @NotNull String target);

    void addDictData(long guildId, @NotNull String target, @NotNull String read);

    void removeDictData(long guildId, @NotNull String target);
}
