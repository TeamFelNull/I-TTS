package dev.felnull.ttsvoice.savedata;

import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.core.savedata.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SelfHostSaveDataManager implements SaveDataAccess {
    private static final SelfHostSaveDataManager INSTANCE = new SelfHostSaveDataManager();
    protected static final File SERVER_DATA_FOLDER = new File("./server_data");
    private final Map<Long, ServerData> serverData = new ConcurrentHashMap<>();

    public static SelfHostSaveDataManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean init() {
        FNDataUtil.wishMkdir(SERVER_DATA_FOLDER);

        var r = Arrays.stream(SERVER_DATA_FOLDER.listFiles()).filter(fw -> !fw.isDirectory()).mapToLong(fw -> {
                    try {
                        return Long.parseLong(FNStringUtil.removeExtension(fw.getName()));
                    } catch (NumberFormatException numberFormatException) {
                        return -1;
                    }
                }).filter(n -> n != -1).mapToObj(n -> {
                    var sdi = new ServerDataImpl(n);
                    serverData.put(n, sdi);
                    return sdi;
                }).map(sdi -> CompletableFuture.runAsync(sdi::loadExistingData, Main.RUNTIME.getHeavyProcessExecutor()))
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(r).join();
        Main.RUNTIME.getLogger().info("{} server data loading success.", r.length);


        return true;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        return serverData.computeIfAbsent(guildId, ServerDataImpl::new);
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
