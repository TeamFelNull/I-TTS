package dev.felnull.itts.core.savedata.dao;

import dev.felnull.itts.core.dict.DictionaryUseEntry;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * データベースアクセス用クラス
 *
 * @author MORIMORI0317
 */
public interface DAO {

    /**
     * 初期化
     */
    void init();

    /**
     * 破棄
     */
    void dispose();

    /**
     * コネクションを取得
     *
     * @return コネクション
     */
    Connection getConnection() throws SQLException;

    /**
     * サーバーテーブル
     *
     * @return テーブルインスタンス
     */
    ServerKeyTable serverKeyTable();

    /**
     * ユーザーテーブル
     *
     * @return テーブルインスタンス
     */
    UserKeyTable userKeyTable();

    /**
     * BOTテーブル
     *
     * @return テーブルインスタンス
     */
    BotKeyTable botKeyTable();

    /**
     * チャンネルテーブル
     *
     * @return テーブルインスタンス
     */
    ChannelKeyTable channelKeyTable();

    /**
     * 辞書テーブル
     *
     * @return テーブルインスタンス
     */
    DictionaryKeyTable dictionaryKeyTable();

    /**
     * 辞書の置き換えタイプテーブル
     *
     * @return テーブルインスタンス
     */
    DictionaryReplaceTypeKeyTable dictionaryReplaceTypeKeyTable();

    /**
     * 自動切断のモードテーブル
     *
     * @return テーブルインスタンス
     */
    AutoDisconnectModeKeyTable autoDisconnectModeKeyTable();

    /**
     * 読み上げ音声タイプテーブル
     *
     * @return テーブルインスタンス
     */
    VoiceTypeKeyTable voiceTypeKeyTable();

    /**
     * サーバー別データテーブル
     *
     * @return テーブルインスタンス
     */
    ServerDataTable serverDataTable();

    /**
     * サーバー別ユーザーデータテーブル
     *
     * @return テーブルインスタンス
     */
    ServerUserDataTable serverUserDataTable();

    /**
     * 辞書利用データテーブル
     *
     * @return テーブルインスタンス
     */
    DictionaryUseDataTable dictionaryUseDataTable();

    /**
     * BOT状態データテーブル
     *
     * @return テーブルインスタンス
     */
    BotStateDataTable botStateDataTable();

    /**
     * サーバー別カスタム辞書データテーブル
     *
     * @return テーブルインスタンス
     */
    ServerCustomDictionaryTable serverCustomDictionaryTable();

    /**
     * 共通カスタム辞書データテーブル
     *
     * @return テーブルインスタンス
     */
    GlobalCustomDictionaryTable globalCustomDictionaryTable();

    /**
     * 絵文字をサポートしているか確認
     *
     * @return サポートしているかどうか
     */
    boolean checkEmojiSupport();

    /**
     * データベースのテーブル
     */
    interface Table {

        /**
         * テーブルを作成
         *
         * @param connection コネクション
         * @throws SQLException エラー
         */
        void createTableIfNotExists(@NotNull Connection connection) throws SQLException;

    }

    /**
     * データのキー用テーブル
     *
     * @param <T> テーブルレコードの型
     */
    interface KeyTable<T> extends Table {

        /**
         * キーからIDを取得
         *
         * @param connection コネクション
         * @param key        キー
         * @return キーID
         */
        OptionalInt selectId(@NotNull Connection connection, @NotNull T key) throws SQLException;

        /**
         * IDからキーを取得
         *
         * @param connection コネクション
         * @param keyId      キーID
         * @return キー
         */
        Optional<T> selectKey(@NotNull Connection connection, int keyId) throws SQLException;

        /**
         * テーブルに指定されたキーが存在しなければ追加する
         *
         * @param connection コネクション
         * @param key        キー
         */
        void insertKeyIfNotExists(@NotNull Connection connection, @NotNull T key) throws SQLException;
    }

    /**
     * サーバテーブル
     */
    interface ServerKeyTable extends KeyTable<Long> {
    }

    /**
     * ユーザーテーブル
     */
    interface UserKeyTable extends KeyTable<Long> {
    }

    /**
     * BOTテーブル
     */
    interface BotKeyTable extends KeyTable<Long> {
    }

    /**
     * チャンネルテーブル
     */
    interface ChannelKeyTable extends KeyTable<Long> {
    }

    /**
     * 辞書テーブル
     */
    interface DictionaryKeyTable extends KeyTable<String> {
    }

    /**
     * 辞書の置き換えタイプテーブル
     */
    interface DictionaryReplaceTypeKeyTable extends KeyTable<String> {
    }

    /**
     * 自動切断のモードテーブル
     */
    interface AutoDisconnectModeKeyTable extends KeyTable<String> {
    }

    /**
     * 読み上げ音声タイプテーブル
     */
    interface VoiceTypeKeyTable extends KeyTable<String> {
    }

    /**
     * データ格納テーブル
     *
     * @param <K> データを取り出すキー
     * @param <T> レコード
     */
    interface DataTable<K extends Record, T extends Record> extends Table {

        /**
         * テーブルに指定されたレコードが存在しなければ追加する
         *
         * @param connection コネクション
         * @param key        キー
         * @param record     レコード
         */
        void insertRecordIfNotExists(@NotNull Connection connection, @NotNull K key, @NotNull T record) throws SQLException;

        /**
         * キーからレコードを取得する
         *
         * @param connection コネクション
         * @param key        キー
         * @return レコード
         */
        Optional<IdRecordPair<T>> selectRecordByKey(@NotNull Connection connection, @NotNull K key) throws SQLException;

        /**
         * IDからレコードを取得する
         *
         * @param connection コネクション
         * @param id         ID
         * @return レコード
         */
        Optional<T> selectRecordById(@NotNull Connection connection, int id) throws SQLException;
    }

    /**
     * サーバー別データテーブル
     */
    interface ServerDataTable extends DataTable<ServerKey, ServerDataRecord> {

        /**
         * "デフォルトの音声タイプのキーID"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return デフォルトの音声タイプのキーID
         * @throws SQLException エラー
         */
        OptionalInt selectDefaultVoiceType(Connection connection, int recordId) throws SQLException;

        /**
         * "デフォルトの音声タイプのキーID"を更新する
         *
         * @param connection            コネクション
         * @param recordId              レコードID
         * @param defaultVoiceTypeKeyId デフォルトの音声タイプのキーID
         * @throws SQLException エラー
         */
        void updateDefaultVoiceType(Connection connection, int recordId, @Nullable Integer defaultVoiceTypeKeyId) throws SQLException;

        /**
         * "無視する正規表現"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 無視する正規表現
         * @throws SQLException エラー
         */
        Optional<String> selectIgnoreRegex(Connection connection, int recordId) throws SQLException;

        /**
         * "無視する正規表現"を更新する
         *
         * @param connection  コネクション
         * @param recordId    レコードID
         * @param ignoreRegex 無視する正規表現
         * @throws SQLException エラー
         */
        void updateIgnoreRegex(Connection connection, int recordId, @Nullable String ignoreRegex) throws SQLException;

        /**
         * "参加時のみ読み上げるかどうか"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 参加時のみ読み上げるかどうか
         * @throws SQLException エラー
         */
        boolean selectNeedJoin(Connection connection, int recordId) throws SQLException;

        /**
         * "参加時のみ読み上げるかどうか"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param needJoin   参加時のみ読み上げるかどうか
         * @throws SQLException エラー
         */
        void updateNeedJoin(Connection connection, int recordId, boolean needJoin) throws SQLException;

        /**
         * "読み上げを上書きするかどうか"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 読み上げを上書きするかどうか
         * @throws SQLException エラー
         */
        boolean selectOverwriteAloud(Connection connection, int recordId) throws SQLException;

        /**
         * "読み上げを上書きするかどうか"を更新する
         *
         * @param connection     コネクション
         * @param recordId       レコードID
         * @param overwriteAloud 読み上げを上書きするかどうか
         * @throws SQLException エラー
         */
        void updateOverwriteAloud(Connection connection, int recordId, boolean overwriteAloud) throws SQLException;

        /**
         * "参加時に読み上げるかどうか"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 参加時に読み上げるかどうか
         * @throws SQLException エラー
         */
        boolean selectNotifyMove(Connection connection, int recordId) throws SQLException;

        /**
         * "参加時に読み上げるかどうか"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param notifyMove 参加時に読み上げるかどうか
         * @throws SQLException エラー
         */
        void updateNotifyMove(Connection connection, int recordId, boolean notifyMove) throws SQLException;

        /**
         * "最大読み上げ数"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 最大読み上げ数
         * @throws SQLException エラー
         */
        int selectReadLimit(Connection connection, int recordId) throws SQLException;

        /**
         * "最大読み上げ数"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param readLimit  最大読み上げ数
         * @throws SQLException エラー
         */
        void updateReadLimit(Connection connection, int recordId, int readLimit) throws SQLException;

        /**
         * "名前の最大読み上げ数"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 名前の最大読み上げ数
         * @throws SQLException エラー
         */
        int selectNameReadLimit(Connection connection, int recordId) throws SQLException;

        /**
         * "名前の最大読み上げ数"を更新する
         *
         * @param connection    コネクション
         * @param recordId      レコードID
         * @param nameReadLimit 名前の最大読み上げ数
         * @throws SQLException エラー
         */
        void updateNameReadLimit(Connection connection, int recordId, int nameReadLimit) throws SQLException;

        /**
         * "自動切断モードのキーID"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 自動切断モードのキーID
         * @throws SQLException エラー
         */
        int selectAutoDisconnectMode(Connection connection, int recordId) throws SQLException;

        /**
         * "自動切断モードのキーID"を更新する
         *
         * @param connection              コネクション
         * @param recordId                レコードID
         * @param autoDisconnectModeKeyId 自動切断モードのキーID
         * @throws SQLException エラー
         */
        void updateAutoDisconnectMode(Connection connection, int recordId, int autoDisconnectModeKeyId) throws SQLException;
    }

    /**
     * サーバー別ユーザーデータテーブル
     */
    interface ServerUserDataTable extends DataTable<ServerUserKey, ServerUserDataRecord> {

        /**
         * "音声タイプのキーID"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 音声タイプのキーID
         * @throws SQLException エラー
         */
        OptionalInt selectVoiceType(Connection connection, int recordId) throws SQLException;

        /**
         * "音声タイプのキーID"を更新する
         *
         * @param connection     コネクション
         * @param recordId       レコードID
         * @param voiceTypeKeyId 音声タイプのキーID
         * @throws SQLException エラー
         */
        void updateVoiceType(Connection connection, int recordId, @Nullable Integer voiceTypeKeyId) throws SQLException;

        /**
         * "拒否情報"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 拒否情報
         * @throws SQLException エラー
         */
        boolean selectDeny(Connection connection, int recordId) throws SQLException;

        /**
         * "拒否情報"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param deny       拒否情報
         * @throws SQLException エラー
         */
        void updateDeny(Connection connection, int recordId, boolean deny) throws SQLException;

        /**
         * "ニックネーム"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return ニックネーム
         * @throws SQLException エラー
         */
        Optional<String> selectNickName(Connection connection, int recordId) throws SQLException;

        /**
         * "ニックネーム"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param nickName   ニックネーム
         * @throws SQLException エラー
         */
        void updateNickName(Connection connection, int recordId, @Nullable String nickName) throws SQLException;

        /**
         * 指定されたサーバーで拒否されているユーザーをすべて取得する
         *
         * @param connection  コネクション
         * @param serverKeyId サーバーキーID
         * @return 拒否されたユーザーのDiscordIDリスト
         * @throws SQLException エラー
         */
        List<Long> selectAllDenyUser(Connection connection, int serverKeyId) throws SQLException;
    }

    /**
     * 辞書利用データテーブル
     */
    interface DictionaryUseDataTable extends DataTable<ServerDictionaryKey, DictionaryUseDataRecord> {

        /**
         * "辞書を有効にしているかどうか"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 辞書を有効にしているかどうか
         * @throws SQLException エラー
         */
        Optional<Boolean> selectEnable(Connection connection, int recordId) throws SQLException;

        /**
         * "辞書を有効にしているかどうか"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param enable     辞書を有効にしているかどうか
         * @throws SQLException エラー
         */
        void updateEnable(Connection connection, int recordId, @Nullable Boolean enable) throws SQLException;

        /**
         * "辞書優先度"を取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 辞書優先度
         * @throws SQLException エラー
         */
        OptionalInt selectPriority(Connection connection, int recordId) throws SQLException;

        /**
         * "辞書優先度"を更新する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @param priority   辞書優先度
         * @throws SQLException エラー
         */
        void updatePriority(Connection connection, int recordId, @Nullable Integer priority) throws SQLException;

        /**
         * 指定されたサーバーの有効もしくはデフォルト状態の辞書をすべて取得する
         *
         * @param connection  コネクション
         * @param serverKeyId サーバーキーのID
         * @return 辞書使用エントリのリスト
         */
        List<DictionaryUseEntry> selectAll(Connection connection, int serverKeyId) throws SQLException;
    }

    /**
     * BOT状態データテーブル
     */
    interface BotStateDataTable extends DataTable<ServerBotKey, BotStateDataRecord> {

        /**
         * 接続されているチェンネルキーのペアを取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 接続されているチェンネルキーのペア
         * @throws SQLException エラー
         */
        Optional<TTSChannelKeyPair> selectConnectedChannelKeyPair(Connection connection, int recordId) throws SQLException;

        /**
         * 接続されているチェンネルを更新する
         *
         * @param connection     コネクション
         * @param recordId       レコードID
         * @param channelKeyPair 接続されているチェンネルキーのペア
         * @throws SQLException エラー
         */
        void updateConnectedChannelKeyPair(Connection connection, int recordId, @Nullable TTSChannelKeyPair channelKeyPair) throws SQLException;

        /**
         * 再接続されるチェンネルキーのペアを取得する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return 再接続されるチェンネルキーのペア
         * @throws SQLException エラー
         */
        Optional<TTSChannelKeyPair> selectReconnectChannelKeyPair(Connection connection, int recordId) throws SQLException;

        /**
         * 再接続されるチェンネルを更新する
         *
         * @param connection     コネクション
         * @param recordId       レコードID
         * @param channelKeyPair 再接続されるチェンネルキーのペア
         * @throws SQLException エラー
         */
        void updateReconnectChannelKeyPair(Connection connection, int recordId, @Nullable TTSChannelKeyPair channelKeyPair) throws SQLException;


        /**
         * 指定されたBOTが接続しているサーバーとチャンネルをすべて取得する
         *
         * @param connection コネクション
         * @param botKeyId   BOTのキーID
         * @return サーバーと接続チャンネルペアのマップ
         * @throws SQLException エラー
         */
        Map<Long, TTSChannelPair> selectAllConnectedChannelPairByBotKeyId(Connection connection, int botKeyId) throws SQLException;

        /**
         * 接続中のオーディオチャンネルキーIDを取得
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return チャンネルキーID
         * @throws SQLException エラー
         */
        OptionalInt selectSpeakAudioChannel(Connection connection, int recordId) throws SQLException;

        /**
         * 接続中のオーディオチャンネルを更新
         *
         * @param connection   コネクション
         * @param recordId     レコードID
         * @param channelKeyId チャンネルキーID
         * @throws SQLException エラー
         */
        void updateSpeakAudioChannel(Connection connection, int recordId, Integer channelKeyId) throws SQLException;

        /**
         * 読み上げるテキストチャンネルを取得
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @return チャンネルキーID
         * @throws SQLException エラー
         */
        OptionalInt selectReadAroundTextChannel(Connection connection, int recordId) throws SQLException;

        /**
         * 読み上げるテキストチャンネルを更新
         *
         * @param connection   コネクション
         * @param recordId     レコードID
         * @param channelKeyId チャンネルキーID
         * @throws SQLException エラー
         */
        void updateReadAroundTextChannel(Connection connection, int recordId, Integer channelKeyId) throws SQLException;
    }

    /**
     * サーバーカスタム辞書テーブル
     */
    interface ServerCustomDictionaryTable extends Table {

        /**
         * 指定されたサーバーの辞書をすべて取得する
         *
         * @param connection コネクション
         * @param key        サーバーキー
         * @return 全レコード
         * @throws SQLException エラー
         */
        @Unmodifiable
        Map<Integer, DictionaryRecord> selectRecords(Connection connection, @NotNull ServerKey key) throws SQLException;

        /**
         * 指定されたサーバーの辞書にレコードを追加する
         *
         * @param connection コネクション
         * @param key        サーバーキー
         * @param record     レコード
         * @throws SQLException エラー
         */
        void insertRecord(Connection connection, @NotNull ServerKey key, @NotNull DictionaryRecord record) throws SQLException;

        /**
         * 指定されたサーバーの辞書からレコードを削除する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @throws SQLException エラー
         */
        void deleteRecord(Connection connection, int recordId) throws SQLException;

        /**
         * 対象の読みのエントリを取得する
         *
         * @param connection コネクション
         * @param key        サーバーキー
         * @param targetWord 読み
         * @return IDと読み
         */
        Map<Integer, DictionaryRecord> selectRecordByTarget(Connection connection, @NotNull ServerKey key, @NotNull String targetWord) throws SQLException;
    }

    /**
     * 共通カスタム辞書テーブル
     */
    interface GlobalCustomDictionaryTable extends Table {

        /**
         * 辞書をすべて取得する
         *
         * @param connection コネクション
         * @return 全レコード
         * @throws SQLException エラー
         */
        @Unmodifiable
        Map<Integer, DictionaryRecord> selectRecords(Connection connection) throws SQLException;

        /**
         * 辞書にレコードを追加する
         *
         * @param connection コネクション
         * @param record     レコード
         * @throws SQLException エラー
         */
        void insertRecord(Connection connection, @NotNull DictionaryRecord record) throws SQLException;

        /**
         * 辞書からレコードを削除する
         *
         * @param connection コネクション
         * @param recordId   レコードID
         * @throws SQLException エラー
         */
        void deleteRecord(Connection connection, int recordId) throws SQLException;

        /**
         * 対象の読みのエントリを取得する
         *
         * @param connection コネクション
         * @param targetWord 読み
         * @return IDと読み
         */
        Map<Integer, DictionaryRecord> selectRecordByTarget(Connection connection, @NotNull String targetWord) throws SQLException;
    }

}
