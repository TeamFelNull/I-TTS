package dev.felnull.itts.core.savedata.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * MySQL用DAO
 */
final class MysqlDAO extends BaseDAO {

    /**
     * ホスト名
     */
    private final String host;

    /**
     * ポート番号
     */
    private final int port;

    /**
     * データベース名
     */
    private final String databaseName;

    /**
     * ユーザー名
     */
    private final String user;

    /**
     * パスワード
     */
    private final String password;

    /**
     * データソース
     */
    private HikariDataSource dataSource;

    /**
     * コンストラクタ
     *
     * @param host         ホスト名
     * @param port         ポート番号
     * @param databaseName データベース名
     * @param user         ユーザー名
     * @param password     パスワード
     */
    MysqlDAO(String host, int port, String databaseName, String user, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }

    @Override
    public void init() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("I-TTS MySQL Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName));
        config.setUsername(user);
        config.setPassword(password);

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() throws IOException {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    private void execute(@NotNull Connection connection, @NotNull @Language("MySQL") String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private void executeCreateIndex(@NotNull Connection connection, @NotNull @Language("MySQL") String procedureSql) throws SQLException {
        connection.setAutoCommit(false);

        try {
            execute(connection, "drop procedure if exists create_idx");
            execute(connection, procedureSql);
            execute(connection, "call create_idx()");
            execute(connection, "drop procedure if exists create_idx");
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void createServerTableIfNotExists(@NotNull Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists server(
                    id integer not null primary key auto_increment, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public int insertAndSelectServer(Connection connection, long discordId) throws SQLException {
        return 0;
    }

    @Override
    public void createUserTableIfNotExists(@NotNull Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists user(
                    id integer not null primary key auto_increment, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createBotTableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists bot(
                    id integer not null primary key auto_increment, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertBotIfNotExists(Connection connection, long discordId) throws SQLException {
        @Language("MySQL")
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
        @Language("MySQL")
        String sql = """
                create table if not exists channel(
                    id integer not null primary key auto_increment, -- ID
                    discord_id bigint not null unique -- DiscordのID
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void createDictionaryTableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists dictionary(
                    id integer not null primary key auto_increment, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertDictionaryIfNotExists(Connection connection, String name) throws SQLException {
        @Language("MySQL")
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
        @Language("MySQL")
        String sql = """
                create table if not exists dictionary_replace_type(
                    id integer not null primary key auto_increment, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public void insertDictionaryReplaceTypeIfNotExists(Connection connection, String name) throws SQLException {
        @Language("MySQL")
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
        @Language("MySQL")
        String sql = """
                create table if not exists auto_disconnect_mode(
                    id integer not null primary key auto_increment, -- ID
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
        return OptionalInt.empty();
    }

    @Override
    public void createVoiceTypeTableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists voice_type(
                    id integer not null primary key auto_increment, -- ID
                    name varchar(30) not null unique -- 参照名
                );
                """;

        execute(connection, sql);
    }

    @Override
    public int insertAndSelectVoiceType(Connection connection, String voiceTypeId) throws SQLException {
        return 0;
    }

    @Override
    public void createServerDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists server_data_v0(
                    id integer not null primary key auto_increment, -- ID
                    server_id integer not null unique, -- サーバーID
                    default_voice_type integer, -- デフォルトの声タイプ
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

    }

    @Override
    public Optional<ServerDataV0Record> selectServerDataV0(Connection connection, long guildId) throws SQLException {
        return Optional.empty();
    }

    @Override
    public Optional<String> selectServerDataV0DefaultVoiceType(Connection connection, long guildId) throws SQLException {
        return Optional.empty();
    }

    @Override
    public void createServerUserDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists server_user_data_v0(
                    id integer not null primary key auto_increment, -- ID
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
        @Language("MySQL")
        String sql = """
                 create procedure create_idx()
                 begin
                    if not exists(SELECT * FROM INFORMATION_SCHEMA.STATISTICS where INDEX_NAME = 'server_user_data_v0_idx') then
                    create unique index server_user_data_v0_idx on server_user_data_v0(server_id, user_id);
                    end if;
                 end;
                """;

        executeCreateIndex(connection, sql);
    }

    @Override
    public void createDictionaryUseDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists dictionary_use_data_v0(
                    id integer not null primary key auto_increment, -- ID
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
        @Language("MySQL")
        String sql = """
                 create procedure create_idx()
                 begin
                    if not exists(SELECT * FROM INFORMATION_SCHEMA.STATISTICS where INDEX_NAME = 'dictionary_use_data_v0_idx') then
                    create unique index dictionary_use_data_v0_idx on dictionary_use_data_v0(server_id);
                    end if;
                 end;
                """;

        executeCreateIndex(connection, sql);
    }

    @Override
    public void createBotStateDataV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists bot_state_data_v0(
                    id integer not null primary key auto_increment, -- ID
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
        @Language("MySQL")
        String sql = """
                 create procedure create_idx()
                 begin
                    if not exists(SELECT * FROM INFORMATION_SCHEMA.STATISTICS where INDEX_NAME = 'bot_state_data_v0_idx') then
                    create unique index bot_state_data_v0_idx on bot_state_data_v0(server_id);
                    end if;
                 end;
                """;

        executeCreateIndex(connection, sql);
    }

    @Override
    public void createServerCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists server_custom_dictionary_v0(
                    id integer not null primary key auto_increment, -- ID
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
        @Language("MySQL")
        String sql = """
                 create procedure create_idx()
                 begin
                    if not exists(SELECT * FROM INFORMATION_SCHEMA.STATISTICS where INDEX_NAME = 'server_custom_dictionary_v0_idx') then
                    create unique index server_custom_dictionary_v0_idx on server_custom_dictionary_v0(server_id);
                    end if;
                 end;
                """;

        executeCreateIndex(connection, sql);
    }

    @Override
    public void createGlobalCustomDictionaryV0TableIfNotExists(Connection connection) throws SQLException {
        @Language("MySQL")
        String sql = """
                create table if not exists global_custom_dictionary_v0(
                    id integer not null primary key auto_increment, -- ID
                    target_word nvarchar(100) not null, -- 置き換え対象の文字
                    read_word nvarchar(100) not null, -- 実際に読み上げる文字
                    replace_type integer not null, -- 置き換え方法
                
                    foreign key (replace_type) references dictionary_replace_type(id)
                );
                """;

        execute(connection, sql);
    }
}
