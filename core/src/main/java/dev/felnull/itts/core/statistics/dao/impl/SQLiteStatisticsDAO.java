package dev.felnull.itts.core.statistics.dao.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.felnull.fnjl.util.FNDataUtil;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * SQLiteの統計DAO実装
 */
class SQLiteStatisticsDAO extends BaseStatisticsDAO {

    /**
     * BOTキーテーブルのインスタンス
     */
    private final BotKeyTable botKeyTable = new BotKeyTableImpl();

    /**
     * サーバーキーテーブルのインスタンス
     */
    private final ServerKeyTable serverKeyTable = new ServerKeyTableImpl();

    /**
     * ボイスタイプテーブルのインスタンス
     */
    private final VoiceTypeTable voiceTypeTable = new VoiceTypeTableImpl();

    /**
     * ボイスカテゴリキーテーブルのインスタンス
     */
    private final VoiceCategoryKeyTable voiceCategoryKeyTable = new VoiceCategoryKeyTableImpl();

    /**
     * 読み上げ文字数集計テーブルのインスタンス
     */
    private final TTSCountTable ttsCountTable = new TTSCountTableImpl();

    /**
     * DBファイル
     */
    private final File dbFile;

    SQLiteStatisticsDAO(@NotNull File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    protected HikariDataSource createDataSource() {
        FNDataUtil.wishMkdir(dbFile.getParentFile());

        HikariConfig config = new HikariConfig();
        config.setPoolName("I-TTS Statistics Pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl(String.format("jdbc:sqlite:%s", dbFile.getAbsolutePath()));

        return new HikariDataSource(config);
    }

    @Override
    public void init() {
        super.init();

        try (Connection connection = getConnection()) {
            execute(connection, "PRAGMA foreign_keys=true");
            execute(connection, "select tbl_name from sqlite_master");
        } catch (SQLException e) {
            throw new RuntimeException("Statistics DB init SQL error", e);
        }
    }

    private void execute(@NotNull Connection connection, @NotNull @Language("SQLite") String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    @Override
    public BotKeyTable botKeyTable() {
        return botKeyTable;
    }

    @Override
    public ServerKeyTable serverKeyTable() {
        return serverKeyTable;
    }

    @Override
    public VoiceTypeTable voiceTypeTable() {
        return voiceTypeTable;
    }

    @Override
    public VoiceCategoryKeyTable voiceCategoryKeyTable() {
        return voiceCategoryKeyTable;
    }

    @Override
    public TTSCountTable ttsCountTable() {
        return ttsCountTable;
    }

    /**
     * BOTキーテーブルの実装
     */
    private final class BotKeyTableImpl implements BotKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = "select discord_id from bot_key where id = ? limit 1;";

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
            String sql = "select id from bot_key where discord_id = ? limit 1;";

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
                        id integer not null primary key autoincrement,
                        discord_id bigint not null unique
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * サーバーキーテーブルの実装
     */
    private final class ServerKeyTableImpl implements ServerKeyTable {

        @Override
        public Optional<Long> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = "select discord_id from server_key where id = ? limit 1;";

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
            String sql = "select id from server_key where discord_id = ? limit 1;";

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
                        id integer not null primary key autoincrement,
                        discord_id bigint not null unique
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * ボイスタイプテーブルの実装
     */
    private final class VoiceTypeTableImpl implements VoiceTypeTable {

        @Override
        public Optional<String> selectName(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = "select name from voice_type where id = ? limit 1;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, keyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("name"));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public OptionalInt selectId(@NotNull Connection connection, @NotNull String name, int categoryKeyId) throws SQLException {
            @Language("SQLite")
            String sql = "select id from voice_type where name = ? and voice_category_id = ? limit 1;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setInt(2, categoryKeyId);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return OptionalInt.of(rs.getInt("id"));
                    }
                }
            }
            return OptionalInt.empty();
        }

        @Override
        public void insertKeyIfNotExists(@NotNull Connection connection, @NotNull String name, int categoryKeyId) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into voice_type(name, voice_category_id)
                    select ?, ? where not exists(select * from voice_type where name = ? and voice_category_id = ?);
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setInt(2, categoryKeyId);
                statement.setString(3, name);
                statement.setInt(4, categoryKeyId);
                statement.execute();
            }
        }

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists voice_type(
                        id integer not null primary key autoincrement,
                        name varchar(75) not null,
                        voice_category_id integer not null default 0,

                        unique(name, voice_category_id)
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * ボイスカテゴリキーテーブルの実装
     */
    private final class VoiceCategoryKeyTableImpl implements VoiceCategoryKeyTable {

        @Override
        public Optional<String> selectKey(@NotNull Connection connection, int keyId) throws SQLException {
            @Language("SQLite")
            String sql = "select name from voice_category_key where id = ? limit 1;";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, keyId);

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
            String sql = "select id from voice_category_key where name = ? limit 1;";

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
                    insert into voice_category_key(name)
                    select ? where not exists(select * from voice_category_key where name = ?);
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
                    create table if not exists voice_category_key(
                        id integer not null primary key autoincrement,
                        name varchar(75) not null unique
                    );
                    """;

            execute(connection, sql);
        }
    }

    /**
     * 読み上げ文字数集計テーブルの実装
     */
    private final class TTSCountTableImpl implements TTSCountTable {

        @Override
        public void createTableIfNotExists(@NotNull Connection connection) throws SQLException {
            @Language("SQLite")
            String sql = """
                    create table if not exists tts_count_data(
                        id integer not null primary key autoincrement,
                        bot_id integer not null,
                        server_id integer not null,
                        voice_type_id integer not null default 0,
                        target_date date not null,
                        spoken_char_count bigint not null default 0,
                        spoken_message_count bigint not null default 0,

                        unique(bot_id, server_id, voice_type_id, target_date),
                        foreign key (bot_id) references bot_key(id),
                        foreign key (server_id) references server_key(id)
                    );
                    """;

            execute(connection, sql);
        }

        @Override
        public void incrementCount(@NotNull Connection connection,
                                   int botKeyId,
                                   int serverKeyId,
                                   int voiceTypeKeyId,
                                   @NotNull LocalDate date,
                                   long charDelta,
                                   long messageDelta) throws SQLException {
            @Language("SQLite")
            String sql = """
                    insert into tts_count_data(bot_id, server_id, voice_type_id, target_date, spoken_char_count, spoken_message_count)
                    values (?, ?, ?, ?, ?, ?)
                    on conflict(bot_id, server_id, voice_type_id, target_date) do update set
                        spoken_char_count = spoken_char_count + excluded.spoken_char_count,
                        spoken_message_count = spoken_message_count + excluded.spoken_message_count
                    """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, botKeyId);
                statement.setInt(2, serverKeyId);
                statement.setInt(3, voiceTypeKeyId);
                statement.setString(4, date.toString());
                statement.setLong(5, charDelta);
                statement.setLong(6, messageDelta);
                statement.execute();
            }
        }

        @Override
        public @NotNull TTSCountSum sumCount(@NotNull Connection connection,
                                             int botKeyId,
                                             @Nullable Integer serverKeyId,
                                             @Nullable Integer voiceTypeKeyId,
                                             @Nullable LocalDate from,
                                             @Nullable LocalDate to) throws SQLException {
            StringBuilder where = new StringBuilder("bot_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(botKeyId);

            if (serverKeyId != null) {
                where.append(" and server_id = ?");
                params.add(serverKeyId);
            }
            if (voiceTypeKeyId != null) {
                where.append(" and voice_type_id = ?");
                params.add(voiceTypeKeyId);
            }
            if (from != null) {
                where.append(" and target_date >= ?");
                params.add(from.toString());
            }
            if (to != null) {
                where.append(" and target_date <= ?");
                params.add(to.toString());
            }

            @Language("SQLite")
            String sql = "select coalesce(sum(spoken_char_count), 0) as char_total, "
                    + "coalesce(sum(spoken_message_count), 0) as message_total "
                    + "from tts_count_data where " + where;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    Object p = params.get(i);
                    if (p instanceof Integer iv) {
                        statement.setInt(i + 1, iv);
                    } else if (p instanceof String sv) {
                        statement.setString(i + 1, sv);
                    }
                }

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return new TTSCountSum(rs.getLong("char_total"), rs.getLong("message_total"));
                    }
                }
            }

            return TTSCountSum.ZERO;
        }
    }
}
