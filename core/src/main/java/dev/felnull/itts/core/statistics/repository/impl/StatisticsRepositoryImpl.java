package dev.felnull.itts.core.statistics.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO.TTSCountSum;
import dev.felnull.itts.core.statistics.dao.StatisticsDAO.TTSCountTable;
import dev.felnull.itts.core.statistics.repository.StatisticsRepoErrorListener;
import dev.felnull.itts.core.statistics.repository.StatisticsRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * 統計レポジトリの実装
 */
public final class StatisticsRepositoryImpl implements StatisticsRepository {

    /**
     * BOTキーのキャッシュ
     */
    private final KeyIdCache<Long> botKeyCache = new KeyIdCache<>(StatisticsDAO::botKeyTable, 10);

    /**
     * サーバーキーのキャッシュ
     */
    private final KeyIdCache<Long> serverKeyCache = new KeyIdCache<>(StatisticsDAO::serverKeyTable, 1000);

    /**
     * ボイスタイプのキャッシュ
     */
    private final VoiceTypeIdCache voiceTypeCache = new VoiceTypeIdCache(100);

    /**
     * ボイスカテゴリキーのキャッシュ
     */
    private final KeyIdCache<String> voiceCategoryKeyCache = new KeyIdCache<>(StatisticsDAO::voiceCategoryKeyTable, 30);

    /**
     * DAO
     */
    private final StatisticsDAO dao;

    /**
     * エラーリスナー
     */
    private final Set<StatisticsRepoErrorListener> errorListeners = new HashSet<>();

    /**
     * 破棄済みフラグ
     */
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * コンストラクタ
     *
     * @param dao 初期化前のDAO
     */
    public StatisticsRepositoryImpl(@NotNull StatisticsDAO dao) {
        this.dao = dao;
    }

    @Override
    public void init() {
        try {
            dao.init();
        } catch (RuntimeException e) {
            throw new RuntimeException("Statistics DAO initialization failure", e);
        }

        try (Connection con = dao.getConnection()) {
            dao.botKeyTable().createTableIfNotExists(con);
            dao.serverKeyTable().createTableIfNotExists(con);
            dao.voiceCategoryKeyTable().createTableIfNotExists(con);
            dao.voiceTypeTable().createTableIfNotExists(con);
            dao.ttsCountTable().createTableIfNotExists(con);
        } catch (SQLException e) {
            throw new IllegalStateException("Statistics database initialization failure", e);
        }
    }

    @Override
    public void dispose() {
        if (destroyed.getAndSet(true)) {
            return;
        }

        try {
            dao.dispose();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close statistics DAO", e);
        }
    }

    @Override
    public void addErrorListener(@NotNull StatisticsRepoErrorListener listener) {
        errorListeners.add(listener);
    }

    @Override
    public void removeErrorListener(@NotNull StatisticsRepoErrorListener listener) {
        errorListeners.remove(listener);
    }

    private void fireErrorEvent(Throwable throwable) {
        if (destroyed.get()) {
            return;
        }
        for (StatisticsRepoErrorListener listener : errorListeners) {
            listener.onError(throwable);
        }
    }

    @Override
    public void increment(long botId,
                          long serverId,
                          @Nullable String voiceTypeId,
                          @Nullable String voiceCategoryId,
                          @NotNull LocalDate date,
                          long charDelta,
                          long messageDelta) {
        try (Connection con = dao.getConnection()) {
            int botKeyId = botKeyCache.getOrInsertId(con, dao, botId);
            int serverKeyId = serverKeyCache.getOrInsertId(con, dao, serverId);
            int voiceCategoryKeyId = voiceCategoryId != null
                    ? voiceCategoryKeyCache.getOrInsertId(con, dao, voiceCategoryId)
                    : TTSCountTable.UNKNOWN_VOICE_KEY_ID;
            int voiceTypeKeyId = voiceTypeId != null
                    ? voiceTypeCache.getOrInsertId(con, dao, voiceTypeId, voiceCategoryKeyId)
                    : TTSCountTable.UNKNOWN_VOICE_KEY_ID;

            dao.ttsCountTable().incrementCount(con, botKeyId, serverKeyId, voiceTypeKeyId, date, charDelta, messageDelta);
        } catch (Exception e) {
            fireErrorEvent(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull TTSCountSum sumCount(long botId,
                                         @Nullable Long serverId,
                                         @Nullable LocalDate from,
                                         @Nullable LocalDate to) {
        try (Connection con = dao.getConnection()) {
            OptionalInt botKeyId = dao.botKeyTable().selectId(con, botId);
            if (botKeyId.isEmpty()) {
                return TTSCountSum.ZERO;
            }

            Integer serverKeyId = null;
            if (serverId != null) {
                OptionalInt sid = dao.serverKeyTable().selectId(con, serverId);
                if (sid.isEmpty()) {
                    return TTSCountSum.ZERO;
                }
                serverKeyId = sid.getAsInt();
            }

            return dao.ttsCountTable().sumCount(con, botKeyId.getAsInt(), serverKeyId, null, from, to);
        } catch (Exception e) {
            fireErrorEvent(e);
            return TTSCountSum.ZERO;
        }
    }

    /**
     * キーIDのキャッシュ
     *
     * @param <T> キーの型
     */
    private static final class KeyIdCache<T> {

        /**
         * テーブル取得関数
         */
        private final Function<StatisticsDAO, StatisticsDAO.KeyTable<T>> tableProvider;

        /**
         * キャッシュ本体
         */
        private final LoadingCache<T, Integer> cache;

        KeyIdCache(Function<StatisticsDAO, StatisticsDAO.KeyTable<T>> tableProvider, int maxSize) {
            this.tableProvider = tableProvider;
            this.cache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .build(new CacheLoader<>() {
                        @Override
                        public @NotNull Integer load(@NotNull T key) {
                            throw new UnsupportedOperationException("Use getOrInsertId(...) instead");
                        }
                    });
        }

        int getOrInsertId(@NotNull Connection con, @NotNull StatisticsDAO dao, @NotNull T key) throws SQLException {
            Integer cached = cache.getIfPresent(key);
            if (cached != null) {
                return cached;
            }

            StatisticsDAO.KeyTable<T> table = tableProvider.apply(dao);
            OptionalInt existing = table.selectId(con, key);
            int id;
            if (existing.isPresent()) {
                id = existing.getAsInt();
            } else {
                table.insertKeyIfNotExists(con, key);
                id = table.selectId(con, key).orElseThrow();
            }
            cache.put(key, id);
            return id;
        }
    }

    /**
     * ボイスタイプの名前とカテゴリキーIDの組
     *
     * @param name          ボイスタイプ名
     * @param categoryKeyId ボイスカテゴリキーID
     */
    private record VoiceTypeKey(@NotNull String name, int categoryKeyId) {
    }

    /**
     * ボイスタイプIDのキャッシュ
     * 名前とカテゴリキーIDの組からIDを解決する
     */
    private static final class VoiceTypeIdCache {

        /**
         * キャッシュ本体
         */
        private final LoadingCache<VoiceTypeKey, Integer> cache;

        VoiceTypeIdCache(int maxSize) {
            this.cache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .build(new CacheLoader<>() {
                        @Override
                        public @NotNull Integer load(@NotNull VoiceTypeKey key) {
                            throw new UnsupportedOperationException("Use getOrInsertId(...) instead");
                        }
                    });
        }

        int getOrInsertId(@NotNull Connection con, @NotNull StatisticsDAO dao, @NotNull String name, int categoryKeyId) throws SQLException {
            VoiceTypeKey key = new VoiceTypeKey(name, categoryKeyId);
            Integer cached = cache.getIfPresent(key);
            if (cached != null) {
                return cached;
            }

            StatisticsDAO.VoiceTypeTable table = dao.voiceTypeTable();
            OptionalInt existing = table.selectId(con, name, categoryKeyId);
            int id;
            if (existing.isPresent()) {
                id = existing.getAsInt();
            } else {
                table.insertKeyIfNotExists(con, name, categoryKeyId);
                id = table.selectId(con, name, categoryKeyId).orElseThrow();
            }
            cache.put(key, id);
            return id;
        }
    }
}
