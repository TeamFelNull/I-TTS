package dev.felnull.itts.core.savedata.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.repository.*;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * データレポジトリの実装
 */
public final class DataRepositoryImpl implements DataRepository {

    /**
     * サーバーキーのデータ
     */
    private final KeyData<Long> serverKeyData = new KeyData<>(this, DAO::serverKeyTable, 1000);

    /**
     * ユーザーキーのデータ
     */
    private final KeyData<Long> userKeyData = new KeyData<>(this, DAO::userKeyTable, 3000);

    /**
     * BOTキーのデータ
     */
    private final KeyData<Long> botKeyData = new KeyData<>(this, DAO::botKeyTable, 10);

    /**
     * チャンネルキーのデータ
     */
    private final KeyData<Long> channelKeyData = new KeyData<>(this, DAO::channelKeyTable, 2000);

    /**
     * 辞書キーのデータ
     */
    private final KeyData<String> dictionaryKeyData = new KeyData<>(this, DAO::dictionaryKeyTable, 100);

    /**
     * 辞書の置き換えタイプキーのデータ
     */
    private final KeyData<String> dictionaryReplaceTypeKeyData = new KeyData<>(this, DAO::dictionaryReplaceTypeKeyTable, 100);


    /**
     * 自動切断モードーのデータ
     */
    private final KeyData<String> autoDisconnectModeKeyData = new KeyData<>(this, DAO::autoDisconnectModeKeyTable, 30);

    /**
     * 音声タイプキーのデータ
     */
    private final KeyData<String> voiceTypeKeyData = new KeyData<>(this, DAO::voiceTypeKeyTable, 100);

    /**
     * サーバーデータのキャッシュ
     */
    private final LoadingCache<Long, ServerData> serverDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull ServerData load(@NotNull Long key) {
                    ServerDataImpl serverData = new ServerDataImpl(DataRepositoryImpl.this, key);
                    serverData.init();
                    return serverData;
                }
            });

    /**
     * サーバー別ユーザーデータのキャッシュ
     */
    private final LoadingCache<ServerUserCacheKey, ServerUserData> serverUserDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull ServerUserData load(@NotNull ServerUserCacheKey key) {
                    ServerUserDataImpl serverUserData = new ServerUserDataImpl(DataRepositoryImpl.this, key.serverId(), key.userId());
                    serverUserData.init();
                    return serverUserData;
                }
            });

    /**
     * 辞書使用データのキャッシュ
     */
    private final LoadingCache<ServerDictionaryCacheKey, DictionaryUseData> dictionaryUseDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull DictionaryUseData load(@NotNull ServerDictionaryCacheKey key) {
                    DictionaryUseDataImpl dictionaryUseData = new DictionaryUseDataImpl(DataRepositoryImpl.this, key.serverId(), key.dictionaryId());
                    dictionaryUseData.init();
                    return dictionaryUseData;
                }
            });

    /**
     * BOT状態データのキャッシュ
     */
    private final LoadingCache<ServerBotCacheKey, BotStateData> botStateDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull BotStateData load(@NotNull ServerBotCacheKey key) {
                    BotStateDataImpl botStateData = new BotStateDataImpl(DataRepositoryImpl.this, key.serverId(), key.botId());
                    botStateData.init();
                    return botStateData;
                }
            });

    /**
     * BOT状態データのキャッシュ
     */
    private final LoadingCache<Long, CustomDictionaryData> serverCustomDictionaryDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull CustomDictionaryData load(@NotNull Long key) {
                    ServerCustomDictionaryData serverCustomDictionaryData = new ServerCustomDictionaryData(DataRepositoryImpl.this, key);
                    serverCustomDictionaryData.init();
                    return serverCustomDictionaryData;
                }
            });

    /**
     * 共通カスタム辞書データ
     */
    private final GlobalCustomDictionaryData globalCustomDictionaryData = new GlobalCustomDictionaryData(DataRepositoryImpl.this);

    /**
     * DAO取得プロバイダ
     */
    private final Supplier<DAO> daoProvider;

    /**
     * データ取得用DAO
     */
    private DAO dao;

    /**
     * コンストラクタ
     *
     * @param daoProvider レポジトリで使用するDAO取得用プロバイダ
     */
    public DataRepositoryImpl(Supplier<DAO> daoProvider) {
        this.daoProvider = daoProvider;
    }


    @Override
    public void init() {
        this.dao = daoProvider.get();

        try {
            this.dao.init();
        } catch (RuntimeException e) {
            throw new RuntimeException("DAO initialization failure", e);
        }

        try {
            initDataBase();
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failure", e);
        }
    }

    private void initDataBase() throws SQLException {
        try (Connection con = dao.getConnection()) {
            // キーテーブル作成
            dao.serverKeyTable().createTableIfNotExists(con);
            dao.userKeyTable().createTableIfNotExists(con);
            dao.botKeyTable().createTableIfNotExists(con);
            dao.channelKeyTable().createTableIfNotExists(con);
            dao.dictionaryKeyTable().createTableIfNotExists(con);
            dao.dictionaryReplaceTypeKeyTable().createTableIfNotExists(con);
            dao.autoDisconnectModeKeyTable().createTableIfNotExists(con);
            dao.voiceTypeKeyTable().createTableIfNotExists(con);

            // データテーブル作成
            dao.serverDataTable().createTableIfNotExists(con);
            dao.serverUserDataTable().createTableIfNotExists(con);
            dao.dictionaryUseDataTable().createTableIfNotExists(con);
            dao.botStateDataTable().createTableIfNotExists(con);
            dao.serverCustomDictionaryTable().createTableIfNotExists(con);
            dao.globalCustomDictionaryTable().createTableIfNotExists(con);
        }
    }

    @Override
    public void dispose() {
        if (this.dao != null) {
            try {
                this.dao.dispose();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to close DAO", e);
            } finally {
                this.dao = null;
            }
        }
    }

    public DAO getDAO() {
        return dao;
    }

    KeyData<Long> getServerKeyData() {
        return serverKeyData;
    }

    KeyData<Long> getUserKeyData() {
        return userKeyData;
    }

    KeyData<Long> getBotKeyData() {
        return botKeyData;
    }

    KeyData<Long> getChannelKeyData() {
        return channelKeyData;
    }

    KeyData<String> getDictionaryKeyData() {
        return dictionaryKeyData;
    }

    KeyData<String> getDictionaryReplaceTypeKeyData() {
        return dictionaryReplaceTypeKeyData;
    }

    KeyData<String> getAutoDisconnectModeKeyData() {
        return autoDisconnectModeKeyData;
    }

    KeyData<String> getVoiceTypeKeyData() {
        return voiceTypeKeyData;
    }

    @Override
    public @NotNull ServerData getServerData(long serverId) {
        return serverDataCache.getUnchecked(serverId);
    }

    @Override
    public @NotNull ServerUserData getServerUserData(long serverId, long userId) {
        return serverUserDataCache.getUnchecked(new ServerUserCacheKey(serverId, userId));
    }

    @Override
    public @NotNull DictionaryUseData getDictionaryUseData(long serverId, String dictionaryId) {
        return dictionaryUseDataCache.getUnchecked(new ServerDictionaryCacheKey(serverId, dictionaryId));
    }

    @Override
    public @NotNull BotStateData getBotStateData(long serverId, long botId) {
        return botStateDataCache.getUnchecked(new ServerBotCacheKey(serverId, botId));
    }

    @Override
    public @NotNull CustomDictionaryData getServerCustomDictionaryData(long serverId) {
        return serverCustomDictionaryDataCache.getUnchecked(serverId);
    }

    @Override
    public @NotNull CustomDictionaryData getGlobalCustomDictionaryData() {
        return globalCustomDictionaryData;
    }

    @Override
    public @NotNull @Unmodifiable Map<Long, TTSChannelPair> getAllConnectedChannel(long botId) {
        try (Connection connection = dao.getConnection()) {
            return dao.botStateDataTable().selectAllConnectedChannelPairByBotKeyId(connection, botKeyData.getId(botId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<Long> getAllDenyUser(long serverId) {
        try (Connection connection = dao.getConnection()) {
            return dao.serverUserDataTable().selectAllDenyUser(connection, serverKeyData.getId(serverId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * サーバーIDとユーザーIDで取得するキャッシュのキー
     *
     * @param serverId サーバーID
     * @param userId   ユーザーID
     */
    private record ServerUserCacheKey(long serverId, long userId) {
    }

    /**
     * サーバーIDと辞書IDで取得するキャッシュのキー
     *
     * @param serverId     サーバーID
     * @param dictionaryId 辞書ID
     */
    private record ServerDictionaryCacheKey(long serverId, String dictionaryId) {
    }

    /**
     * サーバーIDとBOTのIDで取得するキャッシュのキー
     *
     * @param serverId サーバーID
     * @param botId    BOTのID
     */
    private record ServerBotCacheKey(long serverId, long botId) {
    }
}
