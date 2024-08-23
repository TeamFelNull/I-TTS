package dev.felnull.itts.core.savedata.impl;

import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.ServerData;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * サーバーデータの実装
 */
final class ServerDataImpl extends SaveDataBase implements ServerData {

    /**
     * サーバーID
     */
    private final long guildId;

    /**
     * コンストラクタ
     *
     * @param dao DAO
     */
    ServerDataImpl(DAO dao, ErrorCounter errorCounter, long guildId) {
        super(dao, errorCounter);
        this.guildId = guildId;
    }

    @Override
    protected void initRecord(Connection connection) throws SQLException {
        // 既にレコードが存在するか確認
        if (dao.selectServerDataV0(connection, guildId).isPresent()) {
            return;
        }

        // 無ければ追加
        DAO.ServerDataV0Record defaultRecord = new DAO.ServerDataV0Record(INIT_DEFAULT_VOICE_TYPE, INIT_IGNORE_REGEX, INIT_NEED_JOIN, INIT_OVERWRITE_ALOUD,
                INIT_NOTIFY_MOVE, INIT_READ_LIMIT, INIT_NAME_READ_LIMIT, INIT_AUTO_DISCONNECT_MODE);
        dao.insertServerDataV0IfNotExists(connection, guildId, defaultRecord);

        // レコードが正しく追加できているか確認
        if (dao.selectServerDataV0(connection, guildId).isEmpty()) {
            throw new IllegalStateException("Failed to add default data");
        }
    }

    @Override
    public @Nullable String getDefaultVoiceType() {
        return sqlProcWrap(con -> dao.selectServerDataV0DefaultVoiceType(con, guildId).orElse(null));
    }

    @Override
    public void setDefaultVoiceType(@Nullable String voiceType) {
    }

    @Override
    public @Nullable String getIgnoreRegex() {
        return "";
    }

    @Override
    public void setIgnoreRegex(@Nullable String ignoreRegex) {

    }

    @Override
    public boolean isNeedJoin() {
        return false;
    }

    @Override
    public void setNeedJoin(boolean needJoin) {
    }

    @Override
    public boolean isOverwriteAloud() {
        return false;
    }

    @Override
    public void setOverwriteAloud(boolean overwriteAloud) {
    }

    @Override
    public boolean isNotifyMove() {
        return false;
    }

    @Override
    public void setNotifyMove(boolean notifyMove) {
    }

    @Override
    public int getReadLimit() {
        return 0;
    }

    @Override
    public void setReadLimit(int readLimit) {
    }

    @Override
    public int getNameReadLimit() {
        return 0;
    }

    @Override
    public void setNameReadLimit(int nameReadLimit) {
    }

    @Override
    public AutoDisconnectMode getAutoDisconnectMode() {
        return null;
    }

    @Override
    public void setAutoDisconnectMode(AutoDisconnectMode autoDisconnectMode) {

    }

}
