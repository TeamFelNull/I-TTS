package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.dict.DictionaryUseEntry;
import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.repository.impl.DataRepositoryImpl;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

/**
 * データを管理するレポジトリ<br/>
 * 一部を除きスレッドセーフです。
 */
public interface DataRepository {

    /**
     * インスタンス作成
     *
     * @param dao 初期化前のDAO
     * @return データレポジトリインスタンス
     */
    static DataRepository create(DAO dao) {
        return new DataRepositoryImpl(dao);
    }

    /**
     * 初期化
     */
    void init();

    /**
     * 破棄
     */
    void dispose();

    /**
     * エラーリスナーを追加<br/>
     * スレッドセーフではありません。
     *
     * @param errorListener エラーリスナー
     */
    void addErrorListener(RepoErrorListener errorListener);

    /**
     * エラーリスナーを削除<br/>
     * スレッドセーフではありません。
     *
     * @param errorListener エラーリスナー
     */
    void removeErrorListener(RepoErrorListener errorListener);

    /**
     * サーバーデータを取得
     *
     * @param serverId 　サーバーID
     * @return サーバーデータのインスタンス
     */
    @NotNull
    ServerData getServerData(long serverId);

    /**
     * サーバー別ユーザーデータを取得
     *
     * @param serverId サーバーID
     * @param userId   ユーザーID
     * @return サーバー別ユーザーデータのインスタンス
     */
    @NotNull
    ServerUserData getServerUserData(long serverId, long userId);

    /**
     * 辞書使用データを取得
     *
     * @param serverId     サーバーID
     * @param dictionaryId 辞書ID
     * @return 辞書使用データのインスタンス
     */
    @NotNull
    DictionaryUseData getDictionaryUseData(long serverId, String dictionaryId);

    /**
     * BOT状態データを取得
     *
     * @param serverId サーバーID
     * @param botId    BOTのID
     * @return BOT状態データのインスタンス
     */
    @NotNull
    BotStateData getBotStateData(long serverId, long botId);

    /**
     * サーバーのカスタム辞書データを取得
     *
     * @param serverId サーバーID
     * @return サーバーのカスタム辞書データのインスタンス
     */
    @NotNull
    CustomDictionaryData getServerCustomDictionaryData(long serverId);

    /**
     * 共通のカスタム辞書データを取得
     *
     * @return カスタム辞書データのインスタンス
     */
    @NotNull
    CustomDictionaryData getGlobalCustomDictionaryData();

    /**
     * 指定したBOTが接続しているチャンネルをすべて取得する
     *
     * @param botId BOTのID
     * @return サーバーIDと接続チャンネルのmap
     */
    @NotNull
    @Unmodifiable
    Map<Long, TTSChannelPair> getAllConnectedChannel(long botId);

    /**
     * 指定したサーバーのすべての拒否されたユーザーを取得する
     *
     * @param serverId サーバーID
     * @return 拒否されたユーザーIDのリスト
     */
    @NotNull
    @Unmodifiable
    List<Long> getAllDenyUser(long serverId);

    /**
     * 指定されたサーバーの辞書使用データをすべて取得する
     *
     * @param serverId サーバーID
     * @return 辞書使用エントリのリスト
     */
    @NotNull
    @Unmodifiable
    List<DictionaryUseEntry> getAllDictionaryUseData(long serverId);
}
