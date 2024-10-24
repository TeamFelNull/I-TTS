package dev.felnull.itts.core.savedata;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

/**
 * 保存データの管理
 *
 * @author MORIMORI0317
 */
public interface SaveDataManager {

    static SaveDataManager getInstance() {
        return null;
        //   return SaveDataManagerImpl.INSTANCE;
    }

    /**
     * 初期化処理
     */
    void init();

    /**
     * Discordのイベント受信用アダプター
     *
     * @return リスナーアダプター
     */
    ListenerAdapter getDiscordListenerAdapter();

    /**
     * サーバーデータを取得
     *
     * @param guildId サーバーID
     * @return サーバーデータ
     */
    @NotNull
    ServerDataOld getServerData(long guildId);

    /**
     * サーバーごとのユーザデータを取得
     *
     * @param guildId サーバーID
     * @param userId  ユーザID
     * @return サーバーごとのユーザデータ
     */
    @NotNull
    ServerUserDataOld getServerUserData(long guildId, long userId);

    /**
     * 辞書使用データを取得
     *
     * @param guildId サーバーID
     * @param dictId  辞書ID
     * @return 辞書使用データ
     */
    @NotNull
    DictUseDataOld getDictUseData(long guildId, @NotNull String dictId);

    /**
     * ボットの状態データを取得
     *
     * @param guildId サーバーID
     * @return ボットの状態データ
     */
    @NotNull
    BotStateDataOld getBotStateData(long guildId);

    /**
     * 全てのBOT状態データを取得
     *
     * @return 全てのBOTクライアントIDと状態データを含むマップ
     */
    @NotNull
    @Unmodifiable
    Map<Long, BotStateDataOld> getAllBotStateData();

    /**
     * 全てのサーバー辞書データ
     *
     * @param guildId サーバーID
     * @return 全てのサーバー辞書データのリスト
     */
    @NotNull
    @Unmodifiable
    List<DictData> getAllServerDictData(long guildId);

    /**
     * サーバー辞書データを取得
     *
     * @param guildId サーバーID
     * @param target  対象の文字列
     * @return 辞書データ
     */
    @Nullable
    DictData getServerDictData(long guildId, @NotNull String target);

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
    List<DictData> getAllGlobalDictData();

    /**
     * グローバル辞書データを取得
     *
     * @param target 対象の文字列
     * @return グローバル辞書データ
     */
    @Nullable
    DictData getGlobalDictData(@NotNull String target);

    /**
     * グローバル辞書データを追加
     *
     * @param target 対象の文字列
     * @param read   読み
     */
    void addGlobalDictData(@NotNull String target, @NotNull String read);

    /**
     * グローバル辞書データを削除
     *
     * @param target 対象の文字列
     */
    void removeGlobalDictData(@NotNull String target);

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
