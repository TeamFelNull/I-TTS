package dev.felnull.itts.savedata;

import dev.felnull.itts.core.savedata.*;
import dev.felnull.itts.Main;
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
import java.util.stream.Collectors;

public class SelfHostSaveDataManager implements SaveDataAccess {
    private static final SelfHostSaveDataManager INSTANCE = new SelfHostSaveDataManager();
    protected static final File SERVER_DATA_FOLDER = new File("./server_data");
    private static final File SERVER_USER_DATA_PARENT_FOLDER = new File("./server_user_data");
    protected static final File DICT_USE_DATA_FOLDER = new File("./dict_use_data");
    protected static final File BOT_STATE_DATA_FOLDER = new File("./bot_state_data");
    protected static final File SERVER_DICT_FOLDER = new File("./server_dict");
    protected static final File GLOBAL_DICT_DIR = new File("./global_dict.json");
    private final Map<Long, CompletableFuture<ServerDataImpl>> serverData = new ConcurrentHashMap<>();
    private final Map<GuildUserKey, CompletableFuture<ServerUserDataImpl>> serverUserData = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<ServerDictUseData>> serverDictUseData = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<BotStateDataImpl>> botStateData = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<ServerDictData>> serverDict = new ConcurrentHashMap<>();
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
                        botStateData.put(id, computeInitAsync(() -> new BotStateDataImpl(id)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
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
        try {
            return serverDictUseData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictUseData(id))).get().getAllDictUseData();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        try {
            return serverDictUseData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictUseData(id))).get().getDictUseData(dictId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addDictUseData(long guildId, @NotNull String dictId, int priority) {
        try {
            serverDictUseData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictUseData(id))).get().addDictUserData(dictId, priority);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeDictUseData(long guildId, @NotNull String dictId) {
        try {
            serverDictUseData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictUseData(id))).get().removeDictUseData(dictId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull BotStateData getBotStateData(long guildId) {
        try {
            return botStateData.computeIfAbsent(guildId, id -> computeInitAsync(() -> new BotStateDataImpl(id))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull @Unmodifiable Map<Long, BotStateData> getAllBotStateData() {
        return botStateData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            try {
                return entry.getValue().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllServerDictData(long guildId) {
        try {
            return serverDict.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictData(id))).get().getAllDictData();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable DictData getServerDictData(long guildId, @NotNull String target) {
        try {
            return serverDict.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictData(id))).get().getDictData(target);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        try {
            serverDict.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictData(id))).get().addDictData(target, read);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeServerDictData(long guildId, @NotNull String target) {
        try {
            serverDict.computeIfAbsent(guildId, id -> computeInitAsync(() -> new ServerDictData(id))).get().removeDictData(target);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
