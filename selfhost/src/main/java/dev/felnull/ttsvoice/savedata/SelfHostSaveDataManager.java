package dev.felnull.ttsvoice.savedata;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.core.savedata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class SelfHostSaveDataManager implements SaveDataAccess {
    private static final SelfHostSaveDataManager INSTANCE = new SelfHostSaveDataManager();
    protected static final File SERVER_DATA_FOLDER = new File("./server_data");
    private static final File SERVER_USER_DATA_PARENT_FOLDER = new File("./server_user_data");
    private final Map<Long, CompletableFuture<ServerDataImpl>> serverData = new ConcurrentHashMap<>();
    private final Map<GuildUserKey, CompletableFuture<ServerUserDataImpl>> serverUserData = new ConcurrentHashMap<>();

    public static SelfHostSaveDataManager getInstance() {
        return INSTANCE;
    }

    protected static File getServerUserDataFolder(long guildId) {
        return new File(SERVER_USER_DATA_PARENT_FOLDER, String.valueOf(guildId));
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        try {
            return serverData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDataImpl(id))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ServerUserData getServerUserData(long guildId, long userId) {
        try {
            return serverUserData.computeIfAbsent(new GuildUserKey(guildId, userId), id -> computeInitAsync(() -> new ServerUserDataImpl(id.guildId(), id.userId()))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public @Nullable DictData getGlobalDictData(@NotNull String target) {
        return null;
    }

    @Override
    public void addGlobalDictData(@NotNull String target, @NotNull String read) {

    }

    @Override
    public void removeGlobalDictData(@NotNull String target) {

    }

    private <T extends SaveDataBase> CompletableFuture<T> computeInitAsync(Supplier<T> newInstance) {
        return CompletableFuture.supplyAsync(() -> {
            var ni = newInstance.get();
            try {
                ni.init();
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to initialize save data ({}), This data will not be saved.", ni.getName(), ex);
            }
            return ni;
        }, Main.RUNTIME.getAsyncWorkerExecutor());
    }

    private static record GuildUserKey(long guildId, long userId) {
    }
}
