package dev.felnull.itts.core.savedata.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.config.DataBaseConfig;
import dev.felnull.itts.core.dict.Dictionary;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

/**
 * 保存データ管理の実装
 *
 * @author MORIMORI0317
 */
public final class SaveDataManagerImpl implements SaveDataManager {

    /**
     * ロガー
     */
    public static final Logger LOGGER = LogManager.getLogger(SaveDataManagerImpl.class);

    /**
     * インスタンス
     */
    public static final SaveDataManagerImpl INSTANCE = new SaveDataManagerImpl();

    /**
     * SQLiteのDBファイル
     */
    private static final File SQLITE_DB_FILE = new File("./save_data.db");

    /**
     * イベント受信用アダプター
     */
    private final DiscordEventAdaptor discordEventAdaptor = new DiscordEventAdaptor();

    private final ErrorCounter errorCounter = new ErrorCounter(() -> {

    });

    /**
     * サーバーデータのキャッシュ
     */
    private final LoadingCache<Long, ServerDataImpl> serverDataCache = createSaveDataCache(1000, (arg, key) -> new ServerDataImpl(arg.getLeft(), arg.getRight(), key));

    /**
     * DAO
     */
    private DAO dao;

    public void test() {
        ServerData serverData = getServerData(364364);
        System.out.println(serverData.getDefaultVoiceType());
    }

    @Override
    public void init() {
        LOGGER.info("Initializing save data manager");

        DataBaseConfig config = KariDataBaseConfig.INSTANCE;
        DataBaseConfig.DataBaseType type = config.getType();

        // DAO作成
        if (type == DataBaseConfig.DataBaseType.SQLITE) {
            this.dao = new SQLiteDAO(SQLITE_DB_FILE);
        } else if (type == DataBaseConfig.DataBaseType.MYSQL) {
            this.dao = new MysqlDAO(config.getHost(), config.getPort(), config.getDatabaseName(), config.getUser(), config.getPassword());
        } else {
            throw new IllegalStateException("Unsupported Database Types");
        }
        this.dao.init();

        // DAOを閉じるフックを登録
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.dao.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close DAO", e);
            }
        }));

        try {
            initDataBase(this.dao);
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failure", e);
        }
    }

    private void initDataBase(DAO dao) throws SQLException {
        try (Connection con = dao.getConnection()) {

            // 共通テーブル登録
            dao.createServerTableIfNotExists(con);
            dao.createUserTableIfNotExists(con);
            dao.createBotTableIfNotExists(con);
            dao.createChannelTableIfNotExists(con);
            dao.createDictionaryTableIfNotExists(con);
            dao.createDictionaryReplaceTypeTableIfNotExists(con);
            dao.createAutoDisconnectModeTableIfNotExists(con);
            dao.createVoiceTypeTableIfNotExists(con);

            // V0テーブル登録
            dao.createServerDataV0TableIfNotExists(con);
            dao.createServerUserDataV0TableIfNotExists(con);
            dao.createServerUserDataV0TableIndexIfNotExists(con);
            dao.createDictionaryUseDataV0TableIfNotExists(con);
            dao.createDictionaryUseDataV0TableIndexIfNotExists(con);
            dao.createBotStateDataV0TableIfNotExists(con);
            dao.createBotStateDataV0TableIndexIfNotExists(con);
            dao.createServerCustomDictionaryV0TableIfNotExists(con);
            dao.createServerCustomDictionaryV0TableIndexIfNotExists(con);
            dao.createGlobalCustomDictionaryV0TableIfNotExists(con);

            // プログラムと同期を取るレコードを追加
            for (ReplaceType replaceType : ReplaceType.values()) {
                dao.insertDictionaryReplaceTypeIfNotExists(con, replaceType.getName());
            }

            for (AutoDisconnectMode autoDisconnectMode : AutoDisconnectMode.values()) {
                dao.insertAutoDisconnectModeIfNotExists(con, autoDisconnectMode.getName());
            }

            DictionaryManager dictManager = ITTSRuntime.getInstance().getDictionaryManager();
            for (Dictionary dict : dictManager.getAllDictionaries()) {
                dao.insertDictionaryIfNotExists(con, dict.getId());
            }

            JDA jda = ITTSRuntime.getInstance().getBot().getJDA();
            if (jda != null) {
                dao.insertBotIfNotExists(con, jda.getSelfUser().getIdLong());
            }
        }
    }

    private <K, E extends SaveDataBase> LoadingCache<K, E> createSaveDataCache(int size, BiFunction<Pair<DAO, ErrorCounter>, K, E> saveDataProvider) {
        return CacheBuilder.newBuilder()
                .maximumSize(size)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull E load(@NotNull K key) {
                        E data = saveDataProvider.apply(Pair.of(dao, errorCounter), key);
                        data.init();
                        return data;
                    }
                });
    }

    @Override
    public ListenerAdapter getDiscordListenerAdapter() {
        return this.discordEventAdaptor;
    }

    @Override
    public @NotNull ServerData getServerData(long guildId) {
        try {
            return serverDataCache.get(guildId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ServerUserData getServerUserData(long guildId, long userId) {
        return null;
    }

    @Override
    public @NotNull DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return null;
    }

    @Override
    public @NotNull BotStateData getBotStateData(long guildId) {
        return null;
    }

    @Override
    public @NotNull @Unmodifiable Map<Long, BotStateData> getAllBotStateData() {
        return Map.of();
    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllServerDictData(long guildId) {
        return List.of();
    }

    @Override
    public @Nullable DictData getServerDictData(long guildId, @NotNull String target) {
        return null;
    }

    @Override
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {

    }

    @Override
    public void removeServerDictData(long guildId, @NotNull String target) {

    }

    @Override
    public @NotNull @Unmodifiable List<DictData> getAllGlobalDictData() {
        return List.of();
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

    @Override
    public @NotNull @Unmodifiable List<Long> getAllDenyUser(long guildId) {
        return List.of();
    }

    private void registerBotId(JDA jda) {
        // DBにBOTのIDを登録
        try (Connection con = dao.getConnection()) {
            dao.insertBotIfNotExists(con, jda.getSelfUser().getIdLong());
        } catch (SQLException ex) {
            LOGGER.error("Failed to register BOT ID in database", ex);
        }
    }

    private class DiscordEventAdaptor extends ListenerAdapter {
        @Override
        public void onReady(@NotNull ReadyEvent event) {
            registerBotId(event.getJDA());
        }

        @Override
        public void onSessionResume(@NotNull SessionResumeEvent event) {
            registerBotId(event.getJDA());
        }
    }
}
