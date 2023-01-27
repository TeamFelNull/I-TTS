package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public class SaveDataManager {
    private final SaveDataAccess saveDataAccess;

    public SaveDataManager(SaveDataAccess saveDataAccess) {
        this.saveDataAccess = saveDataAccess;
    }

    public boolean init() {
        if (!saveDataAccess.init())
            return false;

        return true;
    }

    @NotNull
    public ServerData getServerData(long guildId) {
        return saveDataAccess.getServerData(guildId);
    }

    @NotNull
    public ServerUserData getServerUserData(long guildId, long userId) {
        return saveDataAccess.getServerUserData(guildId, userId);
    }

    @NotNull
    @Unmodifiable
    public List<DictUseData> getAllDictUseData(long guildId) {
        return saveDataAccess.getAllDictUseData(guildId);
    }

    @Nullable
    public DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return saveDataAccess.getDictUseData(guildId, dictId);
    }

    public void addDictUseData(long guildId, @NotNull String dictId, int priority) {
        saveDataAccess.addDictUseData(guildId, dictId, priority);
    }

    public void removeDictUseData(long guildId, @NotNull String dictId) {
        saveDataAccess.removeDictUseData(guildId, dictId);
    }

    @NotNull
    public BotStateData getBotStateData(long guildId) {
        return saveDataAccess.getBotStateData(guildId);
    }

    @NotNull
    @Unmodifiable
    public Map<Long, BotStateData> getAllBotStateData() {
        return saveDataAccess.getAllBotStateData();
    }

    @NotNull
    @Unmodifiable
    public List<DictData> getAllDictData(long guildId) {
        return saveDataAccess.getAllDictData(guildId);
    }

    @Nullable
    public DictData getDictData(long guildId, @NotNull String target) {
        return saveDataAccess.getDictData(guildId, target);
    }

    public void addDictData(long guildId, @NotNull String target, @NotNull String read) {
        saveDataAccess.addDictData(guildId, target, read);
    }

    public void removeDictData(long guildId, @NotNull String target) {
        saveDataAccess.removeDictData(guildId, target);
    }

    @Nullable
    public DictData getGlobalDictData(@NotNull String target) {
        return saveDataAccess.getGlobalDictData(target);
    }

    public void addGlobalDictData(@NotNull String target, @NotNull String read) {
        saveDataAccess.addGlobalDictData(target, read);
    }

    public void removeGlobalDictData(@NotNull String target) {
        saveDataAccess.removeGlobalDictData(target);
    }
}
