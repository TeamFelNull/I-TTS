package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.ITTSBaseManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * セーブデータの管理
 *
 * @author MORIMORI0317
 */
public class SaveDataManager implements ITTSBaseManager {

    /**
     * セーブデータへのアクセス
     */
    private final SaveDataAccess saveDataAccess;

    /**
     * コンストラクタ
     *
     * @param saveDataAccess セーブデータへのアクセス
     */
    public SaveDataManager(SaveDataAccess saveDataAccess) {
        this.saveDataAccess = saveDataAccess;
    }

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.runAsync(() -> {
            if (!saveDataAccess.init()) {
                throw new RuntimeException("Failed to initialize");
            }

            getITTSLogger().info("Save data setup complete");
        }, getAsyncExecutor());
    }

    /**
     * 全てのサーバーデータを取得
     *
     * @param guildId サーバーID
     * @return サーバーデータ
     */
    @NotNull
    public ServerData getServerData(long guildId) {
        return saveDataAccess.getServerData(guildId);
    }

    /**
     * サーバーごとのユーザデータを取得
     *
     * @param guildId サーバーID
     * @param userId  ユーザID
     * @return サーバーごとのユーザデータ
     */
    @NotNull
    public ServerUserData getServerUserData(long guildId, long userId) {
        return saveDataAccess.getServerUserData(guildId, userId);
    }

    /**
     * 辞書使用データを取得
     *
     * @param guildId サーバーID
     * @param dictId  辞書ID
     * @return 辞書使用データ
     */
    @NotNull
    public DictUseData getDictUseData(long guildId, @NotNull String dictId) {
        return saveDataAccess.getDictUseData(guildId, dictId);
    }

    /**
     * BOT状態データを取得
     *
     * @param guildId サーバーID
     * @return BOT状態データ
     */
    @NotNull
    public BotStateData getBotStateData(long guildId) {
        return saveDataAccess.getBotStateData(guildId);
    }

    /**
     * 全てのBOT状態データを取得
     *
     * @return 全てのBOTクライアントIDと状態データを含むマップ
     */
    @NotNull
    @Unmodifiable
    public Map<Long, BotStateData> getAllBotStateData() {
        return saveDataAccess.getAllBotStateData();
    }

    /**
     * 全てのサーバー辞書データ
     *
     * @param guildId サーバーID
     * @return 全てのサーバー辞書データのリスト
     */
    @NotNull
    @Unmodifiable
    public List<DictData> getAllServerDictData(long guildId) {
        return saveDataAccess.getAllServerDictData(guildId);
    }

    /**
     * サーバー辞書データを取得
     *
     * @param guildId サーバーID
     * @param target  対象の文字列
     * @return 辞書データ
     */
    @Nullable
    public DictData getServerDictData(long guildId, @NotNull String target) {
        return saveDataAccess.getServerDictData(guildId, target);
    }

    /**
     * サーバー辞書データを追加
     *
     * @param guildId 辞書ID
     * @param target  対象の文字列
     * @param read    読み
     */
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        saveDataAccess.addServerDictData(guildId, target, read);
    }

    /**
     * サーバー辞書データを削除
     *
     * @param guildId サーバーID
     * @param target  対象の文字列
     */
    public void removeServerDictData(long guildId, @NotNull String target) {
        saveDataAccess.removeServerDictData(guildId, target);
    }

    /**
     * 全てのグローバル辞書データを取得
     *
     * @return 全てのグローバル辞書データのリスト
     */
    @NotNull
    @Unmodifiable
    public List<DictData> getAllGlobalDictData() {
        return saveDataAccess.getAllGlobalDictData();
    }

    /**
     * グローバル辞書データを取得
     *
     * @param target 対象の文字列
     * @return グローバル辞書データ
     */
    @Nullable
    public DictData getGlobalDictData(@NotNull String target) {
        return saveDataAccess.getGlobalDictData(target);
    }

    /**
     * グローバル辞書データを追加
     *
     * @param target 対象の文字列
     * @param read   読み
     */
    public void addGlobalDictData(@NotNull String target, @NotNull String read) {
        saveDataAccess.addGlobalDictData(target, read);
    }

    /**
     * グローバル辞書データを削除
     *
     * @param target 対象の文字列
     */
    public void removeGlobalDictData(@NotNull String target) {
        saveDataAccess.removeGlobalDictData(target);
    }

    /**
     * 全ての読み上げ拒否ユーザを取得
     *
     * @param guildId サーバーID
     * @return 全ての読み上げを拒否されたユーザのIDリスト
     */
    @NotNull
    @Unmodifiable
    public List<Long> getAllDenyUser(long guildId) {
        return saveDataAccess.getAllDenyUser(guildId);
    }
}
