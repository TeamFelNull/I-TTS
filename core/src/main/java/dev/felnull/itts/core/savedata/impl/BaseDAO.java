package dev.felnull.itts.core.savedata.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

/**
 * DAOのベースクラス
 */
abstract class BaseDAO implements DAO {

    /**
     * サーバーテーブルIDのキャッシュ
     */
    private final Cache<Long, Integer> serverIdCache = createIdCache(1000);

    /**
     * 音声タイプIDのキャッシュ
     */
    private final Cache<String, Integer> voiceTypeIdCache = createIdCache(100);

    /**
     * 自動切断モードIDのキャッシュ
     */
    private final Map<AutoDisconnectMode, Integer> autoDisconnectIdCache = new EnumMap<>(AutoDisconnectMode.class);

    /**
     * DiscordIdからテーブルのIDに変換するためのキャッシュを作成
     *
     * @return キャッシュ
     */
    private <K, E> Cache<K, E> createIdCache(int size) {
        return CacheBuilder.newBuilder()
                .maximumSize(size)
                .build();
    }

    /**
     * DiscordのサーバーIDからテーブルのIDを取得
     *
     * @param connection コネクション
     * @param guildId    DiscordのサーバーID
     * @return テーブルのID
     */
    protected final int getServerTableId(Connection connection, long guildId) {
        try {
            return serverIdCache.get(guildId, () -> insertAndSelectServer(connection, guildId));
        } catch (ExecutionException e) {
            throw new RuntimeException("Execution error", e);
        }
    }

    /**
     * 音声タイプIDからテーブルのIDを取得
     *
     * @param connection  コネクション
     * @param voiceTypeId 音声タイプID
     * @return テーブルのID
     */
    protected final int getVoiceTypeTableId(Connection connection, @NotNull String voiceTypeId) {
        try {
            return voiceTypeIdCache.get(voiceTypeId, () -> insertAndSelectVoiceType(connection, voiceTypeId));
        } catch (ExecutionException e) {
            throw new RuntimeException("Execution error", e);
        }
    }

    /**
     * 自動切断モードからテーブルのIDを取得
     *
     * @param connection         コネクション
     * @param autoDisconnectMode 自動切断モード
     * @return テーブルのID
     */
    protected final int getAutoDisconnectModeTableId(Connection connection, AutoDisconnectMode autoDisconnectMode) {
        return autoDisconnectIdCache.computeIfAbsent(autoDisconnectMode, mode -> {
            try {
                return selectAutoDisconnectModeId(connection, autoDisconnectMode.getName()).orElseThrow();
            } catch (SQLException | NoSuchElementException e) {
                throw new RuntimeException("Failed to get ID", e);
            }
        });
    }
}
