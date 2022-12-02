package dev.felnull.ttsvoice.savedata;

import dev.felnull.ttsvoice.core.savedata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class SelfHostSaveDataManager implements SaveDataAccess {
    private static final SelfHostSaveDataManager INSTANCE=new SelfHostSaveDataManager();

    public static SelfHostSaveDataManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        return null;
    }

    @Override
    public @NotNull UserData getServerUserData(long guildId, long userId) {
        return null;
    }

    @Override
    public @NotNull @Unmodifiable List<DictUseData> getAllDictUseData(long guildId) {
        return null;
    }

    @Override
    public @Nullable DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return null;
    }

    @Override
    public void addDictUseData(long guildId, @NotNull String dictId, int priority) {

    }

    @Override
    public void removeDictUseData(long guildId, @NotNull String dictId) {

    }

    @Override
    public @NotNull BotServerData getBotServerData(long botUserId, long guildId) {
        return null;
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllDictData(long guildId) {
        return null;
    }

    @Override
    public @Nullable DictData getDictData(long guildId, @NotNull String target) {
        return null;
    }

    @Override
    public void addDictData(long guildId, @NotNull String target, @NotNull String read) {

    }

    @Override
    public void removeDictData(long guildId, @NotNull String target) {

    }
}
