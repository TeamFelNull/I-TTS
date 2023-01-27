package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public interface SaveDataAccess {
    boolean init();

    @NotNull
    ServerData getServerData(long guildId);

    @NotNull
    ServerUserData getServerUserData(long guildId, long userId);

    @NotNull
    @Unmodifiable
    List<DictUseData> getAllDictUseData(long guildId);

    @Nullable
    DictUseData getDictUseData(long guildId, @NotNull String dictId);

    void addDictUseData(long guildId, @NotNull String dictId, int priority);

    void removeDictUseData(long guildId, @NotNull String dictId);

    @NotNull
    BotStateData getBotStateData(long guildId);

    @NotNull
    @Unmodifiable
    Map<Long, BotStateData> getAllBotStateData();

    @NotNull
    @Unmodifiable
    List<DictData> getAllServerDictData(long guildId);

    @Nullable
    DictData getServerDictData(long guildId, @NotNull String target);

    void addServerDictData(long guildId, @NotNull String target, @NotNull String read);

    void removeServerDictData(long guildId, @NotNull String target);

    @NotNull
    @Unmodifiable
    List<DictData> getAllGlobalDictData();

    @Nullable
    DictData getGlobalDictData(@NotNull String target);

    void addGlobalDictData(@NotNull String target, @NotNull String read);

    void removeGlobalDictData(@NotNull String target);
}
