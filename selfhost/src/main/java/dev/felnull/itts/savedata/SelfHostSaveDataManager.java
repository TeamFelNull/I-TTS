package dev.felnull.itts.savedata;

import dev.felnull.itts.Main;
import dev.felnull.itts.core.savedata.*;
import org.apache.commons.lang3.tuple.Pair;
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
    private static final File BOT_STATE_DATA_FOLDER = new File("./save_data/bot_state");
    private static final File GLOBAL_DICT_DIR = new File("./global_dict.json");
    private final KeySaveDataManage<LongSaveDataKey, ServerDataImpl> serverData = new KeySaveDataManage<>(new File("./save_data/server"), ServerDataImpl::new);
    private final KeySaveDataManage<LongSaveDataKey, ServerUsersData> serverUsersData = new KeySaveDataManage<>(new File("./save_data/server_users"), ServerUsersData::new);
    private final KeySaveDataManage<LongSaveDataKey, ServerDictUseData> serverDictUseData = new KeySaveDataManage<>(new File("./save_data/dict_use"), ServerDictUseData::new);
    private final KeySaveDataManage<LongSaveDataKey, BotStateDataImpl> botStateData = new KeySaveDataManage<>(new File("./save_data/bot_state"), BotStateDataImpl::new);
    private final KeySaveDataManage<LongSaveDataKey, ServerDictData> serverDict = new KeySaveDataManage<>(new File("./save_data/server_dict"), ServerDictData::new);
    private CompletableFuture<GlobalDictData> globalDict;

    public static SelfHostSaveDataManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean init() {
        globalDict = globalDictComputeInitAsync(GlobalDictData::new);

        if (BOT_STATE_DATA_FOLDER.exists()) {
            var files = BOT_STATE_DATA_FOLDER.listFiles();
            if (files != null) {
                for (File file : files) {
                    var name = file.getName();
                    if (name.length() <= ".json".length()) continue;
                    try {
                        long id = Long.parseLong(name.substring(0, name.length() - ".json".length()));
                        botStateData.load(new LongSaveDataKey(id));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        return serverData.get(new LongSaveDataKey(guildId));
    }

    @Override
    public @NotNull ServerUserData getServerUserData(long guildId, long userId) {
        return serverUsersData.get(new LongSaveDataKey(guildId)).getUserData(userId);
    }

    @Override
    public @NotNull @Unmodifiable List<DictUseData> getAllDictUseData(long guildId) {
        return serverDictUseData.get(new LongSaveDataKey(guildId)).getAllDictUseData();
    }

    @Override
    public @Nullable DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return serverDictUseData.get(new LongSaveDataKey(guildId)).getDictUseData(dictId);
    }

    @Override
    public void addDictUseData(long guildId, @NotNull String dictId, int priority) {
        serverDictUseData.get(new LongSaveDataKey(guildId)).addDictUserData(dictId, priority);
    }

    @Override
    public void removeDictUseData(long guildId, @NotNull String dictId) {
        serverDictUseData.get(new LongSaveDataKey(guildId)).removeDictUseData(dictId);
    }

    @Override
    public @NotNull BotStateData getBotStateData(long guildId) {
        return botStateData.get(new LongSaveDataKey(guildId));
    }

    @Override
    public @NotNull @Unmodifiable Map<Long, BotStateData> getAllBotStateData() {
        return botStateData.getAllLoaded().entrySet().stream()
                .map(r -> Pair.of(r.getKey().id(), r.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllServerDictData(long guildId) {
        return serverDict.get(new LongSaveDataKey(guildId)).getAllDictData();
    }

    @Override
    public @Nullable DictData getServerDictData(long guildId, @NotNull String target) {
        return serverDict.get(new LongSaveDataKey(guildId)).getDictData(target);
    }

    @Override
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        serverDict.get(new LongSaveDataKey(guildId)).addDictData(target, read);
    }

    @Override
    public void removeServerDictData(long guildId, @NotNull String target) {
        serverDict.get(new LongSaveDataKey(guildId)).removeDictData(target);
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

    private <T extends SaveDataBase> CompletableFuture<T> globalDictComputeInitAsync(Supplier<T> newInstance) {
        return CompletableFuture.supplyAsync(() -> {
            var ni = newInstance.get();
            try {
                ni.init(GLOBAL_DICT_DIR, null);
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to initialize save data ({}), This data will not be saved.", ni.getName(), ex);
            }
            return ni;
        }, Main.RUNTIME.getAsyncWorkerExecutor());
    }

    @Override
    public @NotNull @Unmodifiable List<Long> getAllDenyUser(long guildId) {
        ServerUsersData sud = serverUsersData.get(new LongSaveDataKey(guildId));
        return sud.getAllUserData().entrySet().stream()
                .filter(r -> r.getValue().isDeny())
                .map(Map.Entry::getKey)
                .toList();
    }
}
