package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.dao.ServerDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerKey;
import dev.felnull.itts.core.savedata.repository.ServerData;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * サーバーデータの実装
 */
final class ServerDataImpl extends RecordData<ServerKey, ServerDataRecord> implements ServerData {

    /**
     * 初期状態のデフォルト音声タイプ
     */
    private static final String INITIAL_DEFAULT_VOICE_TYPE = null;

    /**
     * 初期状態で無視する正規表現
     */
    private static final String INITIAL_IGNORE_REGEX = "(!|/|\\\\$|`).*";

    /**
     * 初期状態で参加時のみ読み上げを行うかどうか
     */
    private static final boolean INITIAL_NEED_JOIN = false;

    /**
     * 初期状態で読み上げを上書きするか
     */
    private static final boolean INITIAL_OVERWRITE_ALOUD = false;

    /**
     * 初期状態で参加時に読み上げるかどうか
     */
    private static final boolean INITIAL_NOTIFY_MOVE = true;

    /**
     * 初期状態の読み上げ数
     */
    private static final int INITIAL_READ_LIMIT = 200;

    /**
     * 初期状態の名前読み上げ数
     */
    private static final int INITIAL_NAME_READ_LIMIT = 20;

    /**
     * 初期状態の自動切断モード
     */
    private static final AutoDisconnectMode INITIAL_AUTO_DISCONNECT_MODE = AutoDisconnectMode.OFF;

    /**
     * サーバーID
     */
    private final long serverId;


    ServerDataImpl(DataRepositoryImpl repository, long serverId) {
        super(repository);
        this.serverId = serverId;
    }

    @Override
    protected RecordData<ServerKey, ServerDataRecord>.InitRecordContext getInitRecordContext() {
        int serverKyeId = repository.getServerKeyData().getId(serverId);
        ServerKey serverKey = new ServerKey(serverKyeId);

        return new InitRecordContext(dao().serverDataTable(), serverKey, () -> {
            OptionalInt defaultVoiceTypeKeyId = repository.getVoiceTypeKeyData().getIdNullable(INITIAL_DEFAULT_VOICE_TYPE);
            int autoDisconnectModeKeyId = repository.getAutoDisconnectModeKeyData().getId(INITIAL_AUTO_DISCONNECT_MODE.getName());

            return new ServerDataRecord(
                    defaultVoiceTypeKeyId.isPresent() ? defaultVoiceTypeKeyId.getAsInt() : null,
                    INITIAL_IGNORE_REGEX,
                    INITIAL_NEED_JOIN,
                    INITIAL_OVERWRITE_ALOUD,
                    INITIAL_NOTIFY_MOVE,
                    INITIAL_READ_LIMIT,
                    INITIAL_NAME_READ_LIMIT,
                    autoDisconnectModeKeyId
            );
        });
    }

    @Override
    public @Nullable String getDefaultVoiceType() {
        return sqlProcReturnable(con -> {
            OptionalInt voiceType = dao().serverDataTable().selectDefaultVoiceType(con, recordId());
            if (voiceType.isPresent()) {
                return repository.getVoiceTypeKeyData().getKey(voiceType.getAsInt());
            } else {
                return null;
            }
        });
    }

    @Override
    public void setDefaultVoiceType(@Nullable String voiceType) {
        sqlProc(con -> {
            OptionalInt voiceTypeKeyId = repository.getVoiceTypeKeyData().getIdNullable(voiceType);
            dao().serverDataTable().updateDefaultVoiceType(con, recordId(), voiceTypeKeyId.isPresent() ? voiceTypeKeyId.getAsInt() : null);
        });
    }

    @Override
    public @Nullable String getIgnoreRegex() {
        return sqlProcReturnable(con ->
                dao().serverDataTable().selectIgnoreRegex(con, recordId()).orElse(null)
        );
    }

    @Override
    public void setIgnoreRegex(@Nullable String ignoreRegex) {
        sqlProc(con -> dao().serverDataTable().updateIgnoreRegex(con, recordId(), ignoreRegex));
    }

    @Override
    public boolean isNeedJoin() {
        return sqlProcReturnable(con -> dao().serverDataTable().selectNeedJoin(con, recordId()));
    }

    @Override
    public void setNeedJoin(boolean needJoin) {
        sqlProc(con -> dao().serverDataTable().updateNeedJoin(con, recordId(), needJoin));
    }

    @Override
    public boolean isOverwriteAloud() {
        return sqlProcReturnable(con -> dao().serverDataTable().selectOverwriteAloud(con, recordId()));
    }

    @Override
    public void setOverwriteAloud(boolean overwriteAloud) {
        sqlProc(con -> dao().serverDataTable().updateOverwriteAloud(con, recordId(), overwriteAloud));
    }

    @Override
    public boolean isNotifyMove() {
        return sqlProcReturnable(con -> dao().serverDataTable().selectNotifyMove(con, recordId()));
    }

    @Override
    public void setNotifyMove(boolean notifyMove) {
        sqlProc(con -> dao().serverDataTable().updateNotifyMove(con, recordId(), notifyMove));
    }

    @Override
    public int getReadLimit() {
        return sqlProcReturnable(con -> dao().serverDataTable().selectReadLimit(con, recordId()));
    }

    @Override
    public void setReadLimit(int readLimit) {
        sqlProc(con -> dao().serverDataTable().updateReadLimit(con, recordId(), readLimit));
    }

    @Override
    public int getNameReadLimit() {
        return sqlProcReturnable(con -> dao().serverDataTable().selectNameReadLimit(con, recordId()));
    }

    @Override
    public void setNameReadLimit(int nameReadLimit) {
        sqlProc(con -> dao().serverDataTable().updateNameReadLimit(con, recordId(), nameReadLimit));
    }

    @Override
    public AutoDisconnectMode getAutoDisconnectMode() {
        return sqlProcReturnable(con -> {
            int autoDisMode = dao().serverDataTable().selectAutoDisconnectMode(con, recordId());
            return AutoDisconnectMode.getByName(repository.getAutoDisconnectModeKeyData().getKey(autoDisMode))
                    .orElse(AutoDisconnectMode.OFF);
        });
    }

    @Override
    public void setAutoDisconnectMode(AutoDisconnectMode autoDisconnectMode) {
        sqlProc(con -> {
            int autoDisModeKeyId = repository.getAutoDisconnectModeKeyData().getId(autoDisconnectMode.getName());
            dao().serverDataTable().updateAutoDisconnectMode(con, recordId(), autoDisModeKeyId);
        });
    }

}
