package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.ITTSBaseManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SaveDataManager implements ITTSBaseManager {
    private final SaveDataAccess saveDataAccess;

    public SaveDataManager(SaveDataAccess saveDataAccess) {
        this.saveDataAccess = saveDataAccess;
    }

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.runAsync(() -> {
            if (!saveDataAccess.init())
                throw new RuntimeException("Failed to initialize");

            getITTSLogger().info("Save data setup complete");
        }, getAsyncExecutor());
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
    public List<DictData> getAllServerDictData(long guildId) {
        return saveDataAccess.getAllServerDictData(guildId);
    }

    @Nullable
    public DictData getServerDictData(long guildId, @NotNull String target) {
        return saveDataAccess.getServerDictData(guildId, target);
    }

    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        saveDataAccess.addServerDictData(guildId, target, read);
    }

    public void removeServerDictData(long guildId, @NotNull String target) {
        saveDataAccess.removeServerDictData(guildId, target);
    }

    @NotNull
    @Unmodifiable
    public List<DictData> getAllGlobalDictData() {
        return saveDataAccess.getAllGlobalDictData();
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

    @NotNull
    @Unmodifiable
    public List<Long> getAllDenyUser(long guildId) {
        return saveDataAccess.getAllDenyUser(guildId);
    }
}
