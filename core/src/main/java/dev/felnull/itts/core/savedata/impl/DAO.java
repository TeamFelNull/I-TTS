package dev.felnull.itts.core.savedata.impl;


import dev.felnull.itts.core.discord.AutoDisconnectMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * データベースアクセス用クラス
 *
 * @author MORIMORI0317
 */
interface DAO extends Closeable {

    /**
     * 初期化
     */
    void init();

    /**
     * コネクションを取得
     *
     * @return コネクション
     */
    Connection getConnection() throws SQLException;

    /**
     * サーバー対応テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerTableIfNotExists(Connection connection) throws SQLException;


    /**
     * 指定されたDiscordIDのレコードがあればIDを取得<br/>
     * 無ければ、追加して取得
     *
     * @param connection コネクション
     * @param discordId  DiscordID
     * @return テーブルのID
     */
    int insertAndSelectServer(Connection connection, long discordId) throws SQLException;

    /**
     * ユーザー対応テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createUserTableIfNotExists(Connection connection) throws SQLException;


    /**
     * BOT対応テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createBotTableIfNotExists(Connection connection) throws SQLException;

    /**
     * BOT対応テーブルにレコードを追加する
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void insertBotIfNotExists(Connection connection, long discordId) throws SQLException;

    /**
     * チャンネル対応テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createChannelTableIfNotExists(Connection connection) throws SQLException;

    /**
     * 辞書の種類テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createDictionaryTableIfNotExists(Connection connection) throws SQLException;

    /**
     * 辞書の種類テーブルにレコードを追加する
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void insertDictionaryIfNotExists(Connection connection, String name) throws SQLException;

    /**
     * 辞書の置き換えタイプテーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createDictionaryReplaceTypeTableIfNotExists(Connection connection) throws SQLException;


    /**
     * 辞書の置き換えタイプのレコードを追加する
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void insertDictionaryReplaceTypeIfNotExists(Connection connection, String name) throws SQLException;

    /**
     * 自動切断のモードテーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createAutoDisconnectModeTableIfNotExists(Connection connection) throws SQLException;

    /**
     * 自動切断のモードのレコードを追加する
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void insertAutoDisconnectModeIfNotExists(Connection connection, String name) throws SQLException;

    /**
     * 自動切断モードのIDを取得
     *
     * @param connection コネクション
     * @param name       エラー
     * @return 値
     */
    OptionalInt selectAutoDisconnectModeId(Connection connection, String name) throws SQLException;

    /**
     * 読み上げ音声タイプテーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createVoiceTypeTableIfNotExists(Connection connection) throws SQLException;

    /**
     * 指定された読み上げ音声タイプのレコードがあればIDを取得<br/>
     * 無ければ、追加して取得
     *
     * @param connection  コネクション
     * @param voiceTypeId 音声タイプID
     * @return テーブルのID
     */
    int insertAndSelectVoiceType(Connection connection, String voiceTypeId) throws SQLException;

    /**
     * サーバー別データV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerDataV0TableIfNotExists(Connection connection) throws SQLException;

    /**
     * サーバー別データV0テーブルにレコードを追加する
     *
     * @param connection コネクション
     * @param guildId    サーバーID
     * @param record     追加するレコード
     * @throws SQLException エラー
     */
    void insertServerDataV0IfNotExists(Connection connection, long guildId, ServerDataV0Record record) throws SQLException;

    /**
     * サーバー別データV0テーブルからレコードを取得する
     *
     * @param connection コネクション
     * @param guildId    サーバーID
     * @return レコード
     * @throws SQLException エラー
     */
    Optional<ServerDataV0Record> selectServerDataV0(Connection connection, long guildId) throws SQLException;

    /**
     * サーバー別データV0テーブルからデフォルトの音声タイプを取得する
     *
     * @param connection コネクション
     * @param guildId    サーバーID
     * @return デフォルトの音声タイプ
     * @throws SQLException エラー
     */
    Optional<String> selectServerDataV0DefaultVoiceType(Connection connection, long guildId) throws SQLException;

    /**
     * サーバー別ユーザーデータV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerUserDataV0TableIfNotExists(Connection connection) throws SQLException;

    /**
     * サーバー別ユーザーデータV0テーブルのインデックス作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerUserDataV0TableIndexIfNotExists(Connection connection) throws SQLException;

    /**
     * 辞書利用データV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createDictionaryUseDataV0TableIfNotExists(Connection connection) throws SQLException;

    /**
     * 辞書利用データV0テーブルのインデックス作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createDictionaryUseDataV0TableIndexIfNotExists(Connection connection) throws SQLException;

    /**
     * BOT状態データV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createBotStateDataV0TableIfNotExists(Connection connection) throws SQLException;

    /**
     * BOT状態データV0テーブルのインデックス作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createBotStateDataV0TableIndexIfNotExists(Connection connection) throws SQLException;

    /**
     * サーバー別カスタム辞書データV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException;

    /**
     * サーバー別カスタム辞書データV0テーブルのインデックス作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createServerCustomDictionaryV0TableIndexIfNotExists(Connection connection) throws SQLException;

    /**
     * 共通カスタム辞書データV0テーブル作成クエリを発行
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    void createGlobalCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException;

    record ServerDataV0Record(@Nullable String defaultVoiceTypeId, @Nullable String ignoreRegex, boolean needJoin,
                              boolean overwriteAloud, boolean notifyMove, int readLimit, int nameReadLimit,
                              @NotNull AutoDisconnectMode autoDisconnectMode) {
    }
}
