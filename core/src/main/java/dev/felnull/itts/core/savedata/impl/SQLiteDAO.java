package dev.felnull.itts.core.savedata.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * SQLLite用DAO
 */
final class SQLiteDAO extends BaseDAO {

    /**
     * DBファイル
     */
    private final File dbFile;

    /**
     * データソース
     */
    private HikariDataSource dataSource;

    /**
     * コンストラクタ
     *
     * @param dbFile DBファイル
     */
    SQLiteDAO(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void init() {
        FNDataUtil.wishMkdir(dbFile.getParentFile());

        HikariConfig config = new HikariConfig();
        config.setPoolName("I-TTS Pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

        this.dataSource = new HikariDataSource(config);

        // 外部キー制約を有効化
        try (Connection connection = getConnection()) {
            execute(connection, "PRAGMA foreign_keys=true");
        } catch (SQLException e) {
            throw new RuntimeException("Init SQL error", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    private void execute(@NotNull Connection connection, @NotNull @Language("SQLite") String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void createServerTableIfNotExists(@NotNull Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists server(
                    id integer not null primary key autoincrement, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public int insertAndSelectServer(Connection connection, long discordId) throws SQLException {

        @Language("SQLite")
        String insertSql = """
                insert into server(discord_id)
                select ? where not exists(select * from server where discord_id = ?);
                """;

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setLong(1, discordId);
            statement.setLong(2, discordId);
            statement.execute();
        }

        @Language("SQLite")
        String selectSql = """
                select id from server where discord_id = ? limit 1;
                """;

        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setLong(1, discordId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new IllegalStateException("Could not add or retrieve from table");
    }

    @Override
    public void createUserTableIfNotExists(@NotNull Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists user(
                    id integer not null primary key autoincrement, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createBotTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists bot(
                    id integer not null primary key autoincrement, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertBotIfNotExists(Connection connection, long discordId) throws SQLException {
        @Language("SQLite")
        String sql = """
                insert into bot(discord_id)
                select ? where not exists(select * from bot where discord_id = ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, discordId);
            statement.setLong(2, discordId);
            statement.execute();
        }
    }

    @Override
    public void createChannelTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists channel(
                    id integer not null primary key autoincrement, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public void createDictionaryTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists dictionary(
                    id integer not null primary key autoincrement, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertDictionaryIfNotExists(Connection connection, String name) throws SQLException {
        @Language("SQLite")
        String sql = """
                insert into dictionary(name)
                select ? where not exists(select * from dictionary where name = ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, name);
            statement.execute();
        }
    }

    @Override
    public void createDictionaryReplaceTypeTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists dictionary_replace_type(
                    id integer not null primary key autoincrement, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertDictionaryReplaceTypeIfNotExists(Connection connection, String name) throws SQLException {
        @Language("SQLite")
        String sql = """
                insert into dictionary_replace_type(name)
                select ? where not exists(select * from dictionary_replace_type where name = ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, name);
            statement.execute();
        }
    }

    @Override
    public void createAutoDisconnectModeTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists auto_disconnect_mode(
                    id integer not null primary key autoincrement, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertAutoDisconnectModeIfNotExists(Connection connection, String name) throws SQLException {
        @Language("SQLite")
        String sql = """
                insert into auto_disconnect_mode(name)
                select ? where not exists(select * from auto_disconnect_mode where name = ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, name);
            statement.execute();
        }
    }

    @Override
    public OptionalInt selectAutoDisconnectModeId(Connection connection, String name) throws SQLException {

        @Language("SQLite")
        String sql = """
                select id from auto_disconnect_mode where name = ? limit 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return OptionalInt.of(rs.getInt("id"));
                }
            }
        }

        return OptionalInt.empty();
    }

    @Override
    public void createVoiceTypeTableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists voice_type(
                    id integer not null primary key autoincrement, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public int insertAndSelectVoiceType(Connection connection, String voiceTypeId) throws SQLException {

        @Language("SQLite")
        String insertSql = """
                insert into voice_type(name)
                select ? where not exists(select * from voice_type where name = ?);
                """;

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, voiceTypeId);
            statement.setString(2, voiceTypeId);
            statement.execute();
        }

        @Language("SQLite")
        String selectSql = """
                select id from voice_type where name = ? limit 1;
                """;

        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, voiceTypeId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new IllegalStateException("Could not add or retrieve from table");
    }

    @Override
    public void createServerDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists server_data_v0(
                    id integer not null primary key autoincrement, -- ID
                    server_id integer not null unique, -- サーバーID
                    default_voice_type integer, --デフォルトの声タイプ
                    ignore_regex nvarchar(100), -- 読み上げを無視する正規表現
                    need_join boolean not null, -- VCに参加時のみに読み上げるかどうか
                    overwrite_aloud boolean not null, -- VCに参加時のみに読み上げるかどうか
                    notify_move boolean not null, -- 読み上げを上書きするかどうかｖ
                    read_limit integer not null, -- VC参加時に読み上げるかどうか
                    name_read_limit integer not null, -- 最大読み上げ文字数
                    auto_disconnect_mode integer not null, -- 自動切断のモード
                
                    foreign key (server_id) references server(id),
                    foreign key (default_voice_type) references voice_type(id),
                    foreign key (auto_disconnect_mode) references auto_disconnect_mode(id)
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertServerDataV0IfNotExists(Connection connection, long guildId, ServerDataV0Record record) throws SQLException {
        Objects.requireNonNull(record);

        // https://qiita.com/shakechi/items/c5be910d924b9661c216
        @Language("SQLite")
        String sql = """
                insert into server_data_v0(server_id, default_voice_type, ignore_regex, need_join, overwrite_aloud, notify_move, read_limit, name_read_limit, auto_disconnect_mode)
                select ?, ?, ?, ?, ?, ?, ?, ?, ? where not exists(select * from server_data_v0 where server_id = ?);
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int serverTableId = getServerTableId(connection, guildId);

            String defaultVoiceTypeId = record.defaultVoiceTypeId();

            statement.setInt(1, serverTableId);

            if (defaultVoiceTypeId != null) {
                statement.setInt(2, getVoiceTypeTableId(connection, record.defaultVoiceTypeId()));
            } else {
                statement.setNull(2, Types.INTEGER);
            }

            statement.setString(3, record.ignoreRegex());
            statement.setBoolean(4, record.needJoin());
            statement.setBoolean(5, record.overwriteAloud());
            statement.setBoolean(6, record.notifyMove());
            statement.setInt(7, record.readLimit());
            statement.setInt(8, record.nameReadLimit());
            statement.setInt(9, getAutoDisconnectModeTableId(connection, record.autoDisconnectMode()));
            statement.setInt(10, serverTableId);

            statement.execute();
        }
    }

    @Override
    public Optional<ServerDataV0Record> selectServerDataV0(Connection connection, long guildId) throws SQLException {

        @Language("SQLite")
        String sql = """
                select voice_type.name           as default_voice_name,
                       ignore_regex,
                       need_join,
                       overwrite_aloud,
                       notify_move,
                       read_limit,
                       name_read_limit,
                       auto_disconnect_mode.name as auto_disconnect_mode_name
                 from server_data_v0
                          left join server on server_id = server.id
                          left join  voice_type on default_voice_type = voice_type.id
                          left join auto_disconnect_mode on auto_disconnect_mode = auto_disconnect_mode.id
                 where server.discord_id = ?
                 limit 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, guildId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    ServerDataV0Record record =
                            new ServerDataV0Record(rs.getString("default_voice_name"), rs.getString("ignore_regex"), rs.getBoolean("need_join"), rs.getBoolean("overwrite_aloud"),
                                    rs.getBoolean("notify_move"), rs.getInt("read_limit"), rs.getInt("name_read_limit"), AutoDisconnectMode.getByName(rs.getString("auto_disconnect_mode_name")).orElseThrow());
                    return Optional.of(record);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> selectServerDataV0DefaultVoiceType(Connection connection, long guildId) throws SQLException {

        @Language("SQLite")
        String sql = """
                select voice_type.name           as default_voice_name
                 from server_data_v0
                          left join server on server_id = server.id
                          left join  voice_type on default_voice_type = voice_type.id
                 where server.discord_id = ?
                 limit 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, guildId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("default_voice_name"));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void createServerUserDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists server_user_data_v0(
                    id integer not null primary key autoincrement, -- ID
                    server_id integer not null, -- サーバーID
                    user_id integer not null, -- ユーザーID
                    voice_type integer, -- 声タイプ
                    deny boolean not null, -- 読み上げ拒否されているかどうか
                    nick_name  nvarchar(100), -- ニックネーム
                
                    foreign key (server_id) references server(id),
                    foreign key (user_id) references user(id),
                    foreign key (voice_type) references voice_type(id)
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createServerUserDataV0TableIndexIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create unique index if not exists server_user_data_v0_idx on server_user_data_v0(server_id, user_id)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public void createDictionaryUseDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists dictionary_use_data_v0(
                    id integer not null primary key autoincrement, -- ID
                    server_id integer not null, -- サーバーID
                    dictionary_id integer not null, -- 辞書ID
                    enable boolean, -- 辞書を有効にしているかどうか
                    priority integer not null default 0, -- 優先度
                
                    foreign key (server_id) references server(id),
                    foreign key (dictionary_id) references dictionary(id)
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createDictionaryUseDataV0TableIndexIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create index if not exists dictionary_use_data_v0_idx on dictionary_use_data_v0(server_id)
                """;

        execute(connection, sql);
    }

    @Override
    public void createBotStateDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists bot_state_data_v0(
                    id integer not null primary key autoincrement, -- ID
                    server_id integer not null, -- サーバーID
                    bot_id integer not null, -- BOT ID
                    speak_audio_channel integer, -- 接続オーディオチャンネル
                    read_text_channel integer, -- 読み上げるテキストチャンネル
                    reconnect_speak_audio_channel integer, -- 再接続先オーディオチャンネル
                    reconnect_read_text_channel integer, -- 再接続先読み上げチャンネル
                
                    foreign key (server_id) references server(id),
                    foreign key (bot_id) references bot(id),
                    foreign key (speak_audio_channel) references channel(id),
                    foreign key (read_text_channel) references channel(id),
                    foreign key (reconnect_speak_audio_channel) references channel(id),
                    foreign key (reconnect_read_text_channel) references channel(id)
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createBotStateDataV0TableIndexIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create index if not exists bot_state_data_v0_idx on bot_state_data_v0(server_id)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public void createServerCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists server_custom_dictionary_v0(
                    id integer not null primary key autoincrement, -- ID
                    server_id integer not null, -- サーバーID
                    target_word nvarchar(100) not null, -- 置き換え対象の文字
                    read_word nvarchar(100) not null, -- 実際に読み上げる文字
                    replace_type integer not null, -- 置き換え方法
                
                    foreign key (server_id) references server(id),
                    foreign key (replace_type) references dictionary_replace_type(id)
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createServerCustomDictionaryV0TableIndexIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create index if not exists server_custom_dictionary_v0_idx on server_custom_dictionary_v0(server_id)
                """;

        execute(connection, sql);
    }

    @Override
    public void createGlobalCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("SQLite")
        String sql = """
                create table if not exists global_custom_dictionary_v0(
                    id integer not null primary key autoincrement, -- ID
                    target_word nvarchar(100) not null, -- 置き換え対象の文字
                    read_word nvarchar(100) not null, -- 実際に読み上げる文字
                    replace_type integer not null, -- 置き換え方法
                
                    foreign key (replace_type) references dictionary_replace_type(id)
                );
                """;

        execute(connection, sql);
    }

}
