package dev.felnull.itts.core.savedata.legacy;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.impl.LegacySaveDataLayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * 以前のセーブデータシステムを利用するための互換レイヤー
 */
public interface LegacySaveDataLayer {

    /**
     * インスタンス作成
     *
     * @param saveDataManager セーブデータマネージャー
     * @return 作成されたインスタンス
     */
    static LegacySaveDataLayer create(SaveDataManager saveDataManager) {
        return new LegacySaveDataLayerImpl(saveDataManager);
    }

    /**
     * 全てのサーバーデータを取得
     *
     * @param guildId サーバーID
     * @return サーバーデータ
     */
    @NotNull
    LegacyServerData getServerData(long guildId);

    /**
     * サーバーごとのユーザデータを取得
     *
     * @param guildId サーバーID
     * @param userId  ユーザID
     * @return サーバーごとのユーザデータ
     */
    @NotNull
    LegacyServerUserData getServerUserData(long guildId, long userId);

    /**
     * 全てのサーバー辞書データ
     *
     * @param guildId サーバーID
     * @return 全てのサーバー辞書データのリスト
     */
    @NotNull
    @Unmodifiable
    List<LegacyDictData> getAllServerDictData(long guildId);

    /**
     * サーバー辞書データを取得
     *
     * @param guildId サーバーID
     * @param target  対象の文字列
     * @return 辞書データ
     */
    @Nullable
    LegacyDictData getServerDictData(long guildId, @NotNull String target);

    /**
     * サーバー辞書データを追加
     *
     * @param guildId 辞書ID
     * @param target  対象の文字列
     * @param read    読み
     */
    void addServerDictData(long guildId, @NotNull String target, @NotNull String read);

    /**
     * サーバー辞書データを削除
     *
     * @param guildId サーバーID
     * @param target  対象の文字列
     */
    void removeServerDictData(long guildId, @NotNull String target);

    /**
     * 全てのグローバル辞書データを取得
     *
     * @return 全てのグローバル辞書データのリスト
     */
    @NotNull
    @Unmodifiable
    List<LegacyDictData> getAllGlobalDictData();

    /**
     * 全ての読み上げ拒否ユーザを取得
     *
     * @param guildId サーバーID
     * @return 全ての読み上げを拒否されたユーザのIDリスト
     */
    @NotNull
    @Unmodifiable
    List<Long> getAllDenyUser(long guildId);
}
