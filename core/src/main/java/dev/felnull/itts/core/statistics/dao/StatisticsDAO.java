package dev.felnull.itts.core.statistics.dao;

import dev.felnull.itts.core.statistics.TTSCountRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * 統計データベースアクセス用クラス
 */
public interface StatisticsDAO {

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
     * @throws SQLException エラー
     */
    Connection getConnection() throws SQLException;

    /**
     * BOTキーテーブルを取得
     *
     * @return テーブルインスタンス
     */
    BotKeyTable botKeyTable();

    /**
     * サーバーキーテーブルを取得
     *
     * @return テーブルインスタンス
     */
    ServerKeyTable serverKeyTable();

    /**
     * ボイスタイプキーテーブルを取得
     *
     * @return テーブルインスタンス
     */
    VoiceTypeKeyTable voiceTypeKeyTable();

    /**
     * ボイスカテゴリキーテーブルを取得
     *
     * @return テーブルインスタンス
     */
    VoiceCategoryKeyTable voiceCategoryKeyTable();

    /**
     * 読み上げ文字数集計テーブルを取得
     *
     * @return テーブルインスタンス
     */
    TTSCountTable ttsCountTable();

    /**
     * 集計レコードを構築する
     * 内部用ヘルパー
     *
     * @param botId           BOTのDiscord ID
     * @param serverId        サーバーのDiscord ID
     * @param voiceTypeId     ボイスタイプID
     * @param voiceCategoryId ボイスカテゴリID
     * @param date            集計日
     * @param charCount       文字数
     * @param messageCount    メッセージ数
     * @return レコード
     */
    static TTSCountRecord buildRecord(long botId,
                                      long serverId,
                                      @Nullable String voiceTypeId,
                                      @Nullable String voiceCategoryId,
                                      @NotNull LocalDate date,
                                      long charCount,
                                      long messageCount) {
        return new TTSCountRecord(botId, serverId, voiceTypeId, voiceCategoryId, date, charCount, messageCount);
    }

    /**
     * 統計データベースのテーブル
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
         * @throws SQLException エラー
         */
        OptionalInt selectId(@NotNull Connection connection, @NotNull T key) throws SQLException;

        /**
         * IDからキーを取得
         *
         * @param connection コネクション
         * @param keyId      キーID
         * @return キー
         * @throws SQLException エラー
         */
        Optional<T> selectKey(@NotNull Connection connection, int keyId) throws SQLException;

        /**
         * テーブルに指定されたキーが存在しなければ追加する
         *
         * @param connection コネクション
         * @param key        キー
         * @throws SQLException エラー
         */
        void insertKeyIfNotExists(@NotNull Connection connection, @NotNull T key) throws SQLException;
    }

    /**
     * BOTキーテーブル
     */
    interface BotKeyTable extends KeyTable<Long> {
    }

    /**
     * サーバーキーテーブル
     */
    interface ServerKeyTable extends KeyTable<Long> {
    }

    /**
     * ボイスタイプキーテーブル
     */
    interface VoiceTypeKeyTable extends KeyTable<String> {
    }

    /**
     * ボイスカテゴリキーテーブル
     */
    interface VoiceCategoryKeyTable extends KeyTable<String> {
    }

    /**
     * 読み上げ文字数集計テーブル
     */
    interface TTSCountTable extends Table {

        /**
         * ボイス未指定を表すキーIDの予約値
         */
        int UNKNOWN_VOICE_KEY_ID = 0;

        /**
         * 指定日のカウントを増分する
         *
         * @param connection         コネクション
         * @param botKeyId           BOTキーID
         * @param serverKeyId        サーバーキーID
         * @param voiceTypeKeyId     ボイスタイプキーID 未指定の場合はUNKNOWN_VOICE_KEY_ID
         * @param voiceCategoryKeyId ボイスカテゴリキーID 未指定の場合はUNKNOWN_VOICE_KEY_ID
         * @param date               集計日
         * @param charDelta          文字数の増分
         * @param messageDelta       メッセージ数の増分
         * @throws SQLException エラー
         */
        void incrementCount(@NotNull Connection connection,
                            int botKeyId,
                            int serverKeyId,
                            int voiceTypeKeyId,
                            int voiceCategoryKeyId,
                            @NotNull LocalDate date,
                            long charDelta,
                            long messageDelta) throws SQLException;

        /**
         * 指定条件のカウントを集計して取得する
         * サーバーやボイスにnullを渡すとそのフィルタを無視する
         *
         * @param connection         コネクション
         * @param botKeyId           BOTキーID
         * @param serverKeyId        サーバーキーIDフィルタ nullで全サーバー集計
         * @param voiceTypeKeyId     ボイスタイプキーIDフィルタ nullで全ボイス集計
         * @param voiceCategoryKeyId ボイスカテゴリキーIDフィルタ nullで全カテゴリ集計
         * @param from               開始日 nullで下限なし
         * @param to                 終了日 nullで上限なし
         * @return 文字数とメッセージ数の合計
         * @throws SQLException エラー
         */
        @NotNull
        TTSCountSum sumCount(@NotNull Connection connection,
                             int botKeyId,
                             @Nullable Integer serverKeyId,
                             @Nullable Integer voiceTypeKeyId,
                             @Nullable Integer voiceCategoryKeyId,
                             @Nullable LocalDate from,
                             @Nullable LocalDate to) throws SQLException;
    }

    /**
     * 集計結果
     *
     * @param charCount    文字数の合計
     * @param messageCount メッセージ数の合計
     */
    record TTSCountSum(long charCount, long messageCount) {

        /**
         * ゼロ値
         */
        public static final TTSCountSum ZERO = new TTSCountSum(0L, 0L);
    }
}
