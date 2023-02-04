package dev.felnull.itts.savedata;

import dev.felnull.itts.Main;
import dev.felnull.itts.core.savedata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SelfHostSaveDataManager implements SaveDataAccess {
    private static final SelfHostSaveDataManager INSTANCE = new SelfHostSaveDataManager();
    protected static final File SERVER_DATA_FOLDER = new File("./server_data");
    private static final File SERVER_USER_DATA_PARENT_FOLDER = new File("./server_user_data");
    protected static final File DICT_USE_DATA_FOLDER = new File("./dict_use_data");
    protected static final File BOT_STATE_DATA_FOLDER = new File("./bot_state_data");
    protected static final File SERVER_DICT_FOLDER = new File("./server_dict");
    protected static final File GLOBAL_DICT_DIR = new File("./global_dict.json");
    private final KeySaveDataManage<Long, ServerDataImpl> serverData = new KeySaveDataManage<>(ServerDataImpl::new);
    private final KeySaveDataManage<GuildUserKey, ServerUserDataImpl> serverUserData = new KeySaveDataManage<>(id -> new ServerUserDataImpl(id.guildId(), id.userId()));
    private final KeySaveDataManage<Long, ServerDictUseData> serverDictUseData = new KeySaveDataManage<>(ServerDictUseData::new);
    private final KeySaveDataManage<Long, BotStateDataImpl> botStateData = new KeySaveDataManage<>(BotStateDataImpl::new);
    private final KeySaveDataManage<Long, ServerDictData> serverDict = new KeySaveDataManage<>(ServerDictData::new);
    private CompletableFuture<GlobalDictData> globalDict;

    public static SelfHostSaveDataManager getInstance() {
        return INSTANCE;
    }

    protected static File getServerUserDataFolder(long guildId) {
        return new File(SERVER_USER_DATA_PARENT_FOLDER, String.valueOf(guildId));
    }

    @Override
    public boolean init() {
        globalDict = computeInitAsync(GlobalDictData::new);

        if (BOT_STATE_DATA_FOLDER.exists()) {
            var files = BOT_STATE_DATA_FOLDER.listFiles();
            if (files != null) {
                for (File file : files) {
                    var name = file.getName();
                    if (name.length() <= ".json".length()) continue;
                    try {
                        long id = Long.parseLong(name.substring(0, name.length() - ".json".length()));
                        botStateData.load(id);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        return serverData.get(guildId);
    }

    @Override
    public @NotNull ServerUserData getServerUserData(long guildId, long userId) {
        return serverUserData.get(new GuildUserKey(guildId, userId));
    }

    @Override
    public @NotNull @Unmodifiable List<DictUseData> getAllDictUseData(long guildId) {
        return serverDictUseData.get(guildId).getAllDictUseData();
    }

    @Override
    public @Nullable DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return serverDictUseData.get(guildId).getDictUseData(dictId);
    }

    @Override
    public void addDictUseData(long guildId, @NotNull String dictId, int priority) {
        serverDictUseData.get(guildId).addDictUserData(dictId, priority);
    }

    @Override
    public void removeDictUseData(long guildId, @NotNull String dictId) {
        serverDictUseData.get(guildId).removeDictUseData(dictId);
    }

    @Override
    public @NotNull BotStateData getBotStateData(long guildId) {
        return botStateData.get(guildId);
    }

    @Override
    public @NotNull @Unmodifiable Map<Long, BotStateData> getAllBotStateData() {
        return botStateData.getAllLoaded().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllServerDictData(long guildId) {
        return serverDict.get(guildId).getAllDictData();
    }

    @Override
    public @Nullable DictData getServerDictData(long guildId, @NotNull String target) {
        return serverDict.get(guildId).getDictData(target);
    }

    @Override
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        serverDict.get(guildId).addDictData(target, read);
    }

    @Override
    public void removeServerDictData(long guildId, @NotNull String target) {
        serverDict.get(guildId).removeDictData(target);
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllGlobalDictData() {
        try {
            return globalDict.get().getAllDictData();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable DictData getGlobalDictData(@NotNull String target) {
        try {
            return globalDict.get().getDictData(target);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addGlobalDictData(@NotNull String target, @NotNull String read) {
        try {
            globalDict.get().addDictData(target, read);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeGlobalDictData(@NotNull String target) {
        try {
            globalDict.get().removeDictData(target);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
