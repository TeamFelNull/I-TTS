package dev.felnull.itts.core.savedata.dao.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.core.dict.DictionaryUseEntry;
import dev.felnull.itts.core.savedata.dao.*;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * SQLiteのDAO実装
 */
public class SQLiteDAO extends BaseDAO {

    /**
     * サーバーキーテーブルのインスタンス
     */
    private final ServerKeyTable serverKeyTable = new ServerKeyTableImpl();

    /**
     * ユーザーキーテーブルのインスタンス
     */
    private final UserKeyTable userKeyTable = new UserKeyTableImpl();

    /**
     * BOTキーキーテーブルのインスタンス
     */
    private final BotKeyTable botKeyTable = new BotKeyTableImpl();

    /**
     * チャンネルキーテーブルのインスタンス
     */
    private final ChannelKeyTable channelKeyTable = new ChannelKeyTableImpl();

    /**
     * 辞書キーテーブルのインスタンス
     */
    private final DictionaryKeyTable dictionaryKeyTable = new DictionaryKeyTableImpl();

    /**
     * 辞書置き換えタイプキーテーブルのインスタンス
     */
    private final DictionaryReplaceTypeKeyTable dictionaryReplaceTypeKeyTable = new DictionaryReplaceTypeKeyTableImpl();

    /**
     * 自動切断モードキーテーブルのインスタンス
     */
    private final AutoDisconnectModeKeyTable autoDisconnectModeKeyTable = new AutoDisconnectModeKeyTableImpl();

    /**
     * 読み上げ音声タイプキーテーブルのインスタンス
     */
    private final VoiceTypeKeyTable voiceTypeKeyTable = new VoiceTypeKeyTableImpl();

    /**
     * サーバーデータテーブルのインスタンス
     */
    private final ServerDataTable serverDataTable = new ServerDataTableImpl();

    /**
     * サーバー別ユーザーデータテーブルのインスタンス
     */
    private final ServerUserDataTable serverUserDataTable = new ServerUserDataTableImpl();

    /**
     * 辞書利用データテーブル
     */
    private final DictionaryUseDataTable dictionaryUseDataTable = new DictionaryUseDataTableImpl();

    /**
     * BOT状態データテーブルのインスタンス
     */
    private final BotStateDataTable botStateDataTable = new BotStateDataTableImpl();

    /**
     * サーバーカスタム辞書テーブルのインスタンス
     */
    private final ServerCustomDictionaryTable serverCustomDictionaryTable = new ServerCustomDictionaryTableImpl();

    /**
     * 共通カスタム辞書テーブルのインスタンス
     */
    private final GlobalCustomDictionaryTable globalCustomDictionaryTable = new GlobalCustomDictionaryTableImpl();

    /**
     * DBファイル
     */
    private final File dbFile;

    SQLiteDAO(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    protected HikariDataSource createDataSource() {
        FNDataUtil.wishMkdir(dbFile.getParentFile());

        HikariConfig config = new HikariConfig();
        config.setPoolName("I-TTS Pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl(String.format("jdbc:sqlite:%s", dbFile.getAbsolutePath()));

        return new HikariDataSource(config);
    }

    @Override
    public void init() {
        super.init();

        try (Connection connection = getConnection()) {
            // 外部キー制約を有効化
            execute(connection, "PRAGMA foreign_keys=true");

            // クエリを実行できるか確認
            execute(connection, "select tbl_name from sqlite_master");

        } catch (SQLException e) {
            throw new RuntimeException("Init SQL error", e);
        }
    }

    private void execute(@NotNull Connection connection, @NotNull @Language("SQLite") String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public ServerKeyTable serverKeyTable() {
        return serverKeyTable;
    }

    @Override
    public UserKeyTable userKeyTable() {
        return userKeyTable;
    }

    @Override
    public BotKeyTable botKeyTable() {
        return botKeyTable;
    }

    @Override
    public ChannelKeyTable channelKeyTable() {
        return channelKeyTable;
    }

    @Override
    public DictionaryKeyTable dictionaryKeyTable() {
        return dictionaryKeyTable;
    }

    @Override
    public DictionaryReplaceTypeKeyTable dictionaryReplaceTypeKeyTable() {
        return dictionaryReplaceTypeKeyTable;
    }

    @Override
    public AutoDisconnectModeKeyTable autoDisconnectModeKeyTable() {
        return autoDisconnectModeKeyTable;
    }

    @Override
    public VoiceTypeKeyTable voiceTypeKeyTable() {
        return voiceTypeKeyTable;
    }

    @Override
    public ServerDataTable serverDataTable() {
        return serverDataTable;
    }

    @Override
    public ServerUserDataTable serverUserDataTable() {
        return serverUserDataTable;
    }

    @Override
    public DictionaryUseDataTable dictionaryUseDataTable() {
        return dictionaryUseDataTable;
    }

    @Override
    public BotStateDataTable botStateDataTable() {
        return botStateDataTable;
    }

    @Override
    public ServerCustomDictionaryTable serverCustomDictionaryTable() {
        return serverCustomDictionaryTable;
    }

    @Override
    public GlobalCustomDictionaryTable globalCustomDictionaryTable() {
        return globalCustomDictionaryTable;
    }

    @Override
    public boolean checkEmojiSupport() {
        return true;
    }

    /**
     * サーバーキーテーブルの実装
     */
    private final class ServerKeyTableImpl implements ServerKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select discord_id from server_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("discord_id"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from server_key where discord_id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into server_key(discord_id)
                    select ? where not exists(select * from server_key where discord_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);
                statement.setLong(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists server_key(
                        id integer not null primary key autoincrement, -- ID
                        discord_id bigint not null unique -- DiscordのID
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * ユーザーキーテーブルの実装
     */
    private final class UserKeyTableImpl implements UserKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select discord_id from user_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("discord_id"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from user_key where discord_id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into user_key(discord_id)
                    select ? where not exists(select * from user_key where discord_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);
                statement.setLong(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists user_key(
                        id integer not null primary key autoincrement, -- ID
                        discord_id bigint not null unique -- DiscordのID
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * BOTキーテーブルの実装
     */
    private final class BotKeyTableImpl implements BotKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select discord_id from bot_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("discord_id"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from bot_key where discord_id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into bot_key(discord_id)
                    select ? where not exists(select * from bot_key where discord_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);
                statement.setLong(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists bot_key(
                        id integer not null primary key autoincrement, -- ID
                        discord_id bigint not null unique -- DiscordのID
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * チャンネルキーテーブルの実装
     */
    private final class ChannelKeyTableImpl implements ChannelKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select discord_id from channel_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong("discord_id"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from channel_key where discord_id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull Long key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into channel_key(discord_id)
                    select ? where not exists(select * from channel_key where discord_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key);
                statement.setLong(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists channel_key(
                        id integer not null primary key autoincrement, -- ID
                        discord_id bigint not null unique -- DiscordのID
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * 辞書キーテーブルの実装
     */
    private final class DictionaryKeyTableImpl implements DictionaryKeyTable {

        @Override
        public Optional<String> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select name from dictionary_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from dictionary_key where name = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into dictionary_key(name)
                    select ? where not exists(select * from dictionary_key where name = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);
                statement.setString(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists dictionary_key(
                        id integer not null primary key autoincrement, -- ID
                        name varchar(30) not null unique -- 参照名
                    );
                    """;

            execute(connection, sql);
        }

    }

    /**
     * 辞書置き換えタイプキーテーブルの実装
     */
    private final class DictionaryReplaceTypeKeyTableImpl implements DictionaryReplaceTypeKeyTable {

        @Override
        public Optional<String> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select name from dictionary_replace_type_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from dictionary_replace_type_key where name = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into dictionary_replace_type_key(name)
                    select ? where not exists(select * from dictionary_replace_type_key where name = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);
                statement.setString(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists dictionary_replace_type_key(
                        id integer not null primary key autoincrement, -- ID
                        name varchar(30) not null unique -- 参照名
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * 自動切断のモードテーブルの実装
     */
    private final class AutoDisconnectModeKeyTableImpl implements AutoDisconnectModeKeyTable {

        @Override
        public Optional<String> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select name from auto_disconnect_mode_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from auto_disconnect_mode_key where name = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into auto_disconnect_mode_key(name)
                    select ? where not exists(select * from auto_disconnect_mode_key where name = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);
                statement.setString(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists auto_disconnect_mode_key(
                        id integer not null primary key autoincrement, -- ID
                        name varchar(30) not null unique -- 参照名
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * 読み上げ音声タイプテーブルの実装
     */
    private final class VoiceTypeKeyTableImpl implements VoiceTypeKeyTable {

        @Override
        public Optional<String> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select name from voice_type_key where id = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id from voice_type_key where name = ? limit 1;
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull String key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into voice_type_key(name)
                    select ? where not exists(select * from voice_type_key where name = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key);
                statement.setString(2, key);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists voice_type_key(
                        id integer not null primary key autoincrement, -- ID
                        name varchar(30) not null unique -- 参照名
                    );
                    """;

            execute(connection, sql);
        }

    }

    /**
     * サーバー別データテーブルの実装
     */
    private final class ServerDataTableImpl implements ServerDataTable {

        @Override
        public OptionalInt selectDefaultVoiceType(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select default_voice_type
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer val = (Integer) rs.getObject("default_voice_type");
                        return val == null ? OptionalInt.empty() : OptionalInt.of(val);
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateDefaultVoiceType(Connection connection, int recordId, @Nullable Integer defaultVoiceTypeKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set default_voice_type = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (defaultVoiceTypeKeyId != null) {
                    statement.setInt(1, defaultVoiceTypeKeyId);
                } else {
                    statement.setNull(1, Types.INTEGER);
                }
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public Optional<String> selectIgnoreRegex(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select ignore_regex
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("ignore_regex"));
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateIgnoreRegex(Connection connection, int recordId, @Nullable String ignoreRegex) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set ignore_regex = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, ignoreRegex);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public boolean selectNeedJoin(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select need_join
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("need_join");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateNeedJoin(Connection connection, int recordId, boolean needJoin) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set need_join = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, needJoin);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public boolean selectOverwriteAloud(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select overwrite_aloud
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("overwrite_aloud");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateOverwriteAloud(Connection connection, int recordId, boolean overwriteAloud) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set overwrite_aloud = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setBoolean(1, overwriteAloud);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public boolean selectNotifyMove(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select notify_move
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("notify_move");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateNotifyMove(Connection connection, int recordId, boolean notifyMove) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set notify_move = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setBoolean(1, notifyMove);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public int selectReadLimit(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select read_limit
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("read_limit");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateReadLimit(Connection connection, int recordId, int readLimit) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set read_limit = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, readLimit);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public int selectNameReadLimit(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select name_read_limit
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("name_read_limit");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateNameReadLimit(Connection connection, int recordId, int nameReadLimit) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set name_read_limit = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, nameReadLimit);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public int selectAutoDisconnectMode(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select auto_disconnect_mode
                    from server_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("auto_disconnect_mode");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateAutoDisconnectMode(Connection connection, int recordId, int autoDisconnectModeKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_data
                    set auto_disconnect_mode = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, autoDisconnectModeKeyId);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public void insertRecordIfNotExists(@NotNull Connection connection, @NotNull ServerKey key, @NotNull ServerDataRecord record) throws SQLException {
            Objects.requireNonNull(record);

            // https://qiita.com/shakechi/items/c5be910d924b9661c216
            @Language("SQLite")
            String sql = """
                    insert into server_data(server_id, default_voice_type, ignore_regex, need_join, overwrite_aloud, notify_move,
                               read_limit, name_read_limit, auto_disconnect_mode)
                    select ?, ?, ?, ?, ?, ?, ?, ?, ? where not exists(select * from server_data where server_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, key.serverKeyId());

                if (record.defaultVoiceTypeKeyId() != null) {
                    statement.setInt(2, record.defaultVoiceTypeKeyId());
                } else {
                    statement.setNull(2, Types.INTEGER);
                }

                statement.setString(3, record.ignoreRegex());
                statement.setBoolean(4, record.needJoin());
                statement.setBoolean(5, record.overwriteAloud());
                statement.setBoolean(6, record.notifyMove());
                statement.setInt(7, record.readLimit());
                statement.setInt(8, record.nameReadLimit());
                statement.setInt(9, record.autoDisconnectModeKeyId());
                statement.setInt(10, key.serverKeyId());

                statement.execute();
            }
        }

        @Override
        public Optional<IdRecordPair<ServerDataRecord>> selectRecordByKey(@NotNull Connection connection, @NotNull ServerKey key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           default_voice_type,
                           ignore_regex,
                           need_join,
                           overwrite_aloud,
                           notify_move,
                           read_limit,
                           name_read_limit,
                           auto_disconnect_mode
                     from server_data
                     where server_id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new IdRecordPair<>(rs.getInt("id"), createRecord(rs)));
                    }
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<ServerDataRecord> selectRecordById(@NotNull Connection connection, int id) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select default_voice_type,
                           ignore_regex,
                           need_join,
                           overwrite_aloud,
                           notify_move,
                           read_limit,
                           name_read_limit,
                           auto_disconnect_mode
                     from server_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(createRecord(rs));
                    }
                }
            }

            return Optional.empty();
        }

        private ServerDataRecord createRecord(ResultSet resultSet) throws SQLException {
            return new ServerDataRecord(
                    (Integer) resultSet.getObject("default_voice_type"),
                    resultSet.getString("ignore_regex"),
                    resultSet.getBoolean("need_join"),
                    resultSet.getBoolean("overwrite_aloud"),
                    resultSet.getBoolean("notify_move"),
                    resultSet.getInt("read_limit"),
                    resultSet.getInt("name_read_limit"),
                    resultSet.getInt("auto_disconnect_mode")
            );
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists server_data(
                        id integer not null primary key autoincrement, -- ID
                        server_id integer not null unique, -- サーバーID
                        default_voice_type integer, -- デフォルトの声タイプ
                        ignore_regex nvarchar(100), -- 読み上げを無視する正規表現
                        need_join boolean not null, -- VCに参加時のみに読み上げるかどうか
                        overwrite_aloud boolean not null, -- VCに参加時のみに読み上げるかどうか
                        notify_move boolean not null, -- 読み上げを上書きするかどうかｖ
                        read_limit integer not null, -- VC参加時に読み上げるかどうか
                        name_read_limit integer not null, -- 最大読み上げ文字数
                        auto_disconnect_mode integer not null, -- 自動切断のモード
                    
                        foreign key (server_id) references server_key(id),
                        foreign key (default_voice_type) references voice_type_key(id),
                        foreign key (auto_disconnect_mode) references auto_disconnect_mode_key(id)
                    );
                    """;

            execute(connection, sql);
        }
    }


    /**
     * サーバー別ユーザーデータテーブルの実装
     */
    private final class ServerUserDataTableImpl implements ServerUserDataTable {

        @Override
        public OptionalInt selectVoiceType(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select voice_type
                    from server_user_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer val = (Integer) rs.getObject("voice_type");
                        return val == null ? OptionalInt.empty() : OptionalInt.of(val);
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateVoiceType(Connection connection, int recordId, @Nullable Integer voiceTypeKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_user_data
                    set voice_type = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (voiceTypeKeyId != null) {
                    statement.setInt(1, voiceTypeKeyId);
                } else {
                    statement.setNull(1, Types.INTEGER);
                }
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public boolean selectDeny(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select deny
                    from server_user_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("deny");
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateDeny(Connection connection, int recordId, boolean deny) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_user_data
                    set deny = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setBoolean(1, deny);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public Optional<String> selectNickName(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select nick_name
                    from server_user_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("nick_name"));
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateNickName(Connection connection, int recordId, @Nullable String nickName) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update server_user_data
                    set nick_name = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, nickName);
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public void insertRecordIfNotExists(@NotNull Connection connection, @NotNull ServerUserKey key, @NotNull ServerUserDataRecord record) throws SQLException {
            Objects.requireNonNull(record);

            @Language("SQLite")
            String sql = """
                    insert into server_user_data(server_id, user_id, voice_type, deny, nick_name)
                    select ?, ?, ?, ?, ? where not exists(select * from server_user_data where server_id = ? and user_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, key.serverKeyId());
                statement.setInt(2, key.userKeyId());

                if (record.voiceTypeKeyId() != null) {
                    statement.setInt(3, record.voiceTypeKeyId());
                } else {
                    statement.setNull(3, Types.INTEGER);
                }

                statement.setBoolean(4, record.deny());
                statement.setString(5, record.nickName());

                statement.setInt(6, key.serverKeyId());
                statement.setInt(7, key.userKeyId());

                statement.execute();
            }
        }

        @Override
        public Optional<IdRecordPair<ServerUserDataRecord>> selectRecordByKey(@NotNull Connection connection,
                                                                              @NotNull ServerUserKey key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           voice_type,
                           deny,
                           nick_name
                     from server_user_data
                     where server_id = ? and user_id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());
                statement.setLong(2, key.userKeyId());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new IdRecordPair<>(rs.getInt("id"), createRecord(rs)));
                    }
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<ServerUserDataRecord> selectRecordById(@NotNull Connection connection, int id) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select voice_type,
                           deny,
                           nick_name
                     from server_user_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(createRecord(rs));
                    }
                }
            }

            return Optional.empty();
        }

        private ServerUserDataRecord createRecord(ResultSet resultSet) throws SQLException {
            return new ServerUserDataRecord(
                    (Integer) resultSet.getObject("voice_type"),
                    resultSet.getBoolean("deny"),
                    resultSet.getString("nick_name")
            );
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists server_user_data(
                        id integer not null primary key autoincrement, -- ID
                        server_id integer not null, -- サーバーID
                        user_id integer not null, -- ユーザーID
                        voice_type integer, -- 声タイプ
                        deny boolean not null, -- 読み上げ拒否されているかどうか
                        nick_name  nvarchar(100), -- ニックネーム
                    
                        foreign key (server_id) references server_key(id),
                        foreign key (user_id) references user_key(id),
                        foreign key (voice_type) references voice_type_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public List<Long> selectAllDenyUser(Connection connection, int serverKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select user_key.discord_id as user_discord_id
                    from server_user_data
                        inner join user_key on user_id = user_key.id
                    where server_id = ? and deny = TRUE
                    """;

            ImmutableList.Builder<Long> denyUsersBuilder = ImmutableList.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, serverKeyId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        denyUsersBuilder.add(rs.getLong("user_discord_id"));
                    }
                }
            }

            return denyUsersBuilder.build();
        }
    }

    /**
     * 辞書利用データテーブルの実装
     */
    private final class DictionaryUseDataTableImpl implements DictionaryUseDataTable {

        @Override
        public Optional<Boolean> selectEnable(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select enable
                    from dictionary_use_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Object enableRet = rs.getObject("enable");
                        return Optional.ofNullable(enableRet == null ? null : (Integer) enableRet != 0);
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateEnable(Connection connection, int recordId, @Nullable Boolean enable) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update dictionary_use_data
                    set enable = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (enable != null) {
                    statement.setBoolean(1, enable);
                } else {
                    statement.setNull(1, Types.BOOLEAN);
                }
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public OptionalInt selectPriority(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select priority
                    from dictionary_use_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer val = (Integer) rs.getObject("priority");
                        return val == null ? OptionalInt.empty() : OptionalInt.of(val);
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updatePriority(Connection connection, int recordId, Integer priority) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update dictionary_use_data
                    set priority = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (priority != null) {
                    statement.setInt(1, priority);
                } else {
                    statement.setNull(1, Types.INTEGER);
                }
                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public void insertRecordIfNotExists(@NotNull Connection connection, @NotNull ServerDictionaryKey key,
                                            @NotNull DictionaryUseDataRecord record) throws SQLException {
            Objects.requireNonNull(record);

            @Language("SQLite")
            String sql = """
                    insert into dictionary_use_data(server_id, dictionary_id, enable, priority)
                    select ?, ?, ?, ? where not exists(select * from dictionary_use_data where server_id = ? and dictionary_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, key.serverKeyId());
                statement.setInt(2, key.dictionaryKeyId());

                if (record.enable() != null) {
                    statement.setBoolean(3, record.enable());
                } else {
                    statement.setNull(3, Types.BOOLEAN);
                }

                if (record.priority() != null) {
                    statement.setInt(4, record.priority());
                } else {
                    statement.setNull(4, Types.INTEGER);
                }

                statement.setInt(5, key.serverKeyId());
                statement.setInt(6, key.dictionaryKeyId());

                statement.execute();
            }
        }

        @Override
        public Optional<IdRecordPair<DictionaryUseDataRecord>> selectRecordByKey(@NotNull Connection connection, @NotNull ServerDictionaryKey key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           enable,
                           priority
                     from dictionary_use_data
                     where server_id = ? and dictionary_id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());
                statement.setLong(2, key.dictionaryKeyId());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new IdRecordPair<>(rs.getInt("id"), createRecord(rs)));
                    }
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<DictionaryUseDataRecord> selectRecordById(@NotNull Connection connection, int id) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select enable,
                           priority
                     from dictionary_use_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(createRecord(rs));
                    }
                }
            }

            return Optional.empty();
        }

        private DictionaryUseDataRecord createRecord(ResultSet resultSet) throws SQLException {
            Object enableRet = resultSet.getObject("enable");
            return new DictionaryUseDataRecord(
                    enableRet == null ? null : (Integer) enableRet != 0,
                    (Integer) resultSet.getObject("priority")
            );
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists dictionary_use_data(
                        id integer not null primary key autoincrement, -- ID
                        server_id integer not null, -- サーバーID
                        dictionary_id integer not null, -- 辞書ID
                        enable boolean, -- 辞書を有効にしているかどうか
                        priority integer, -- 優先度
                    
                        foreign key (server_id) references server_key(id),
                        foreign key (dictionary_id) references dictionary_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public List<DictionaryUseEntry> selectAll(Connection connection, int serverKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select dictionary_key.name as dictionary_name,
                           enable,
                           priority
                    from dictionary_use_data
                        inner join dictionary_key on dictionary_id = dictionary_key.id
                    where server_id = ?
                    """;

            List<DictionaryUseEntry> ret = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, serverKeyId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Object enableRet = rs.getObject("enable");
                        ret.add(new DictionaryUseEntry(rs.getString("dictionary_name"),
                                enableRet == null ? null : (Integer) enableRet != 0,
                                (Integer) rs.getObject("priority")));
                    }
                }
            }

            return ret;
        }
    }

    /**
     * BOT状態データテーブルの実装
     */
    private final class BotStateDataTableImpl implements BotStateDataTable {

        @Override
        public Optional<TTSChannelKeyPair> selectConnectedChannelKeyPair(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select speak_audio_channel,
                           read_text_channel
                    from bot_state_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer audioChannel = (Integer) rs.getObject("speak_audio_channel");
                        Integer textChannel = (Integer) rs.getObject("read_text_channel");

                        if (audioChannel == null || textChannel == null) {
                            return Optional.empty();
                        }

                        return Optional.of(new TTSChannelKeyPair(audioChannel, textChannel));
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateConnectedChannelKeyPair(Connection connection, int recordId, @Nullable TTSChannelKeyPair channelKeyPair) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update bot_state_data
                    set speak_audio_channel = ?, read_text_channel = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (channelKeyPair != null) {
                    statement.setLong(1, channelKeyPair.speakAudioChannelKey());
                    statement.setLong(2, channelKeyPair.readTextChannelKey());
                } else {
                    statement.setNull(1, Types.INTEGER);
                    statement.setNull(2, Types.INTEGER);
                }

                statement.setLong(3, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public Optional<TTSChannelKeyPair> selectReconnectChannelKeyPair(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select reconnect_speak_audio_channel,
                           reconnect_read_text_channel
                    from bot_state_data
                    where id = ?
                    limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer audioChannel = (Integer) rs.getObject("reconnect_speak_audio_channel");
                        Integer textChannel = (Integer) rs.getObject("reconnect_read_text_channel");

                        if (audioChannel == null || textChannel == null) {
                            return Optional.empty();
                        }

                        return Optional.of(new TTSChannelKeyPair(audioChannel, textChannel));
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateReconnectChannelKeyPair(Connection connection, int recordId, @Nullable TTSChannelKeyPair channelKeyPair) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update bot_state_data
                    set reconnect_speak_audio_channel = ?, reconnect_read_text_channel = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (channelKeyPair != null) {
                    statement.setLong(1, channelKeyPair.speakAudioChannelKey());
                    statement.setLong(2, channelKeyPair.readTextChannelKey());
                } else {
                    statement.setNull(1, Types.INTEGER);
                    statement.setNull(2, Types.INTEGER);
                }

                statement.setLong(3, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public void insertRecordIfNotExists(@NotNull Connection connection, @NotNull ServerBotKey key, @NotNull BotStateDataRecord record) throws SQLException {
            Objects.requireNonNull(record);

            @Language("SQLite")
            String sql = """
                    insert into bot_state_data(server_id, bot_id, speak_audio_channel, read_text_channel, reconnect_speak_audio_channel, reconnect_read_text_channel)
                    select ?, ?, ?, ?, ?, ? where not exists(select * from bot_state_data where server_id = ? and bot_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, key.serverKeyId());
                statement.setInt(2, key.botKeyId());

                if (record.speakAudioChannelKey() != null) {
                    statement.setInt(3, record.speakAudioChannelKey());
                } else {
                    statement.setNull(3, Types.INTEGER);
                }

                if (record.readTextChannelKey() != null) {
                    statement.setInt(4, record.readTextChannelKey());
                } else {
                    statement.setNull(4, Types.INTEGER);
                }

                if (record.reconnectSpeakAudioChannelKey() != null) {
                    statement.setInt(5, record.reconnectSpeakAudioChannelKey());
                } else {
                    statement.setNull(5, Types.INTEGER);
                }

                if (record.reconnectReadTextChannelKey() != null) {
                    statement.setInt(6, record.reconnectReadTextChannelKey());
                } else {
                    statement.setNull(6, Types.INTEGER);
                }

                statement.setInt(7, key.serverKeyId());
                statement.setInt(8, key.botKeyId());

                statement.execute();
            }
        }

        @Override
        public Optional<IdRecordPair<BotStateDataRecord>> selectRecordByKey(@NotNull Connection connection, @NotNull ServerBotKey key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           speak_audio_channel,
                           read_text_channel,
                           reconnect_speak_audio_channel,
                           reconnect_read_text_channel
                     from bot_state_data
                     where server_id = ? and bot_id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());
                statement.setLong(2, key.botKeyId());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new IdRecordPair<>(rs.getInt("id"), createRecord(rs)));
                    }
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<BotStateDataRecord> selectRecordById(@NotNull Connection connection, int id) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select speak_audio_channel,
                           read_text_channel,
                           reconnect_speak_audio_channel,
                           reconnect_read_text_channel
                     from bot_state_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(createRecord(rs));
                    }
                }
            }

            return Optional.empty();
        }

        private BotStateDataRecord createRecord(ResultSet resultSet) throws SQLException {
            return new BotStateDataRecord(
                    (Integer) resultSet.getObject("speak_audio_channel"),
                    (Integer) resultSet.getObject("read_text_channel"),
                    (Integer) resultSet.getObject("reconnect_speak_audio_channel"),
                    (Integer) resultSet.getObject("reconnect_read_text_channel")
            );
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists bot_state_data(
                        id integer not null primary key autoincrement, -- ID
                        server_id integer not null, -- サーバーID
                        bot_id integer not null, -- BOT ID
                        speak_audio_channel integer, -- 接続オーディオチャンネル
                        read_text_channel integer, -- 読み上げるテキストチャンネル
                        reconnect_speak_audio_channel integer, -- 再接続先オーディオチャンネル
                        reconnect_read_text_channel integer, -- 再接続先読み上げチャンネル
                    
                        foreign key (server_id) references server_key(id),
                        foreign key (bot_id) references bot_key(id),
                        foreign key (speak_audio_channel) references channel_key(id),
                        foreign key (read_text_channel) references channel_key(id),
                        foreign key (reconnect_speak_audio_channel) references channel_key(id),
                        foreign key (reconnect_read_text_channel) references channel_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public Map<Long, TTSChannelPair> selectAllConnectedChannelPairByBotKeyId(Connection connection, int botKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select server_key.discord_id as server_discord_id,
                           speak_audio_channel_key.discord_id as speak_audio_channel_discord_id,
                           read_text_channel_key.discord_id as read_text_channel_discord_id
                     from bot_state_data
                        inner join server_key on server_id = server_key.id
                        inner join channel_key as speak_audio_channel_key on speak_audio_channel = speak_audio_channel_key.id
                        inner join channel_key as read_text_channel_key on read_text_channel = read_text_channel_key.id
                     where bot_id = ? and speak_audio_channel is not null and read_text_channel is not null
                    """;

            ImmutableMap.Builder<Long, TTSChannelPair> retBuilder = ImmutableMap.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, botKeyId);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getObject("speak_audio_channel_discord_id") != null && rs.getObject("read_text_channel_discord_id") != null) {
                            long speakAudioChannel = rs.getLong("speak_audio_channel_discord_id");
                            long readTextChannel = rs.getLong("read_text_channel_discord_id");
                            TTSChannelPair channelPair = new TTSChannelPair(speakAudioChannel, readTextChannel);
                            retBuilder.put(rs.getLong("server_discord_id"), channelPair);
                        }
                    }
                }
            }

            return retBuilder.build();
        }

        @Override
        public OptionalInt selectSpeakAudioChannel(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select speak_audio_channel
                     from bot_state_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer channelKey = (Integer) rs.getObject("speak_audio_channel");
                        return channelKey != null ? OptionalInt.of(channelKey) : OptionalInt.empty();
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateSpeakAudioChannel(Connection connection, int recordId, Integer channelKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update bot_state_data
                    set speak_audio_channel = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (channelKeyId != null) {
                    statement.setLong(1, channelKeyId);
                } else {
                    statement.setNull(1, Types.INTEGER);
                }

                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

        @Override
        public OptionalInt selectReadAroundTextChannel(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select read_text_channel
                     from bot_state_data
                     where id = ?
                     limit 1
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, recordId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Integer channelKey = (Integer) rs.getObject("read_text_channel");
                        return channelKey != null ? OptionalInt.of(channelKey) : OptionalInt.empty();
                    }
                }
            }

            throw new IllegalStateException("Record not found");
        }

        @Override
        public void updateReadAroundTextChannel(Connection connection, int recordId, Integer channelKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    update bot_state_data
                    set read_text_channel = ?
                    where id = ?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                if (channelKeyId != null) {
                    statement.setLong(1, channelKeyId);
                } else {
                    statement.setNull(1, Types.INTEGER);
                }

                statement.setLong(2, recordId);

                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("No record update");
                }
            }
        }

    }

    /**
     * サーバーカスタム辞書テーブルの実装
     */
    private final class ServerCustomDictionaryTableImpl implements ServerCustomDictionaryTable {

        @Override
        public Map<Integer, DictionaryRecord> selectRecords(Connection connection, @NotNull ServerKey key) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           target_word,
                           read_word,
                           replace_type
                    from server_custom_dictionary
                    where server_id = ?
                    """;

            ImmutableMap.Builder<Integer, DictionaryRecord> ret = ImmutableMap.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        DictionaryRecord record =
                                new DictionaryRecord(rs.getString("target_word"), rs.getString("read_word"), rs.getInt("replace_type"));
                        ret.put(rs.getInt("id"), record);
                    }
                }
            }

            return ret.build();
        }

        @Override
        public void insertRecord(Connection connection, @NotNull ServerKey key, @NotNull DictionaryRecord record) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into server_custom_dictionary(server_id, target_word, read_word, replace_type)
                    values(?, ?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, key.serverKeyId());
                statement.setString(2, record.target());
                statement.setString(3, record.read());
                statement.setInt(4, record.replaceTypeKeyId());

                statement.execute();
            }
        }

        @Override
        public void deleteRecord(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    delete from server_custom_dictionary where id=?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, recordId);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists server_custom_dictionary(
                        id integer not null primary key autoincrement, -- ID
                        server_id integer not null, -- サーバーID
                        target_word nvarchar(100) not null, -- 置き換え対象の文字
                        read_word nvarchar(100) not null, -- 実際に読み上げる文字
                        replace_type integer not null, -- 置き換え方法
                    
                        foreign key (server_id) references server_key(id),
                        foreign key (replace_type) references dictionary_replace_type_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public Map<Integer, DictionaryRecord> selectRecordByTarget(Connection connection, @NotNull ServerKey key, @NotNull String targetWord) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           target_word,
                           read_word,
                           replace_type
                    from server_custom_dictionary
                    where server_id = ? and target_word = ?
                    """;

            ImmutableMap.Builder<Integer, DictionaryRecord> ret = ImmutableMap.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, key.serverKeyId());
                statement.setString(2, targetWord);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        DictionaryRecord record =
                                new DictionaryRecord(rs.getString("target_word"), rs.getString("read_word"), rs.getInt("replace_type"));
                        ret.put(rs.getInt("id"), record);
                    }
                }
            }

            return ret.build();
        }
    }

    /**
     * 共通カスタム辞書テーブルの実装
     */
    private final class GlobalCustomDictionaryTableImpl implements GlobalCustomDictionaryTable {

        @Override
        public @Unmodifiable Map<Integer, DictionaryRecord> selectRecords(Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           target_word,
                           read_word,
                           replace_type
                    from global_custom_dictionary
                    """;

            ImmutableMap.Builder<Integer, DictionaryRecord> ret = ImmutableMap.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        DictionaryRecord record =
                                new DictionaryRecord(rs.getString("target_word"), rs.getString("read_word"), rs.getInt("replace_type"));
                        ret.put(rs.getInt("id"), record);
                    }
                }
            }

            return ret.build();
        }

        @Override
        public void insertRecord(Connection connection, @NotNull DictionaryRecord record) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into global_custom_dictionary(target_word, read_word, replace_type)
                    values(?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, record.target());
                statement.setString(2, record.read());
                statement.setInt(3, record.replaceTypeKeyId());

                statement.execute();
            }
        }

        @Override
        public void deleteRecord(Connection connection, int recordId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    delete from global_custom_dictionary where id=?
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, recordId);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists global_custom_dictionary(
                        id integer not null primary key autoincrement, -- ID
                        target_word nvarchar(100) not null, -- 置き換え対象の文字
                        read_word nvarchar(100) not null, -- 実際に読み上げる文字
                        replace_type integer not null, -- 置き換え方法
                    
                        foreign key (replace_type) references dictionary_replace_type_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public Map<Integer, DictionaryRecord> selectRecordByTarget(Connection connection, @NotNull String targetWord) throws SQLException {
            @Language("SQLite")
            String sql = """
                    select id,
                           target_word,
                           read_word,
                           replace_type
                    from global_custom_dictionary
                    where target_word = ?
                    """;

            ImmutableMap.Builder<Integer, DictionaryRecord> ret = ImmutableMap.builder();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, targetWord);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        DictionaryRecord record =
                                new DictionaryRecord(rs.getString("target_word"), rs.getString("read_word"), rs.getInt("replace_type"));
                        ret.put(rs.getInt("id"), record);
                    }
                }
            }

            return ret.build();
        }
    }
}