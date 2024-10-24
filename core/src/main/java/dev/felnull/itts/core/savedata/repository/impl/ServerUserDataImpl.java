package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.ServerUserDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerUserKey;
import dev.felnull.itts.core.savedata.repository.ServerUserData;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * サーバー別ユーザデータの実装
 */
final class ServerUserDataImpl extends RecordData<ServerUserKey, ServerUserDataRecord> implements ServerUserData {

    /**
     * 初期状態の音声タイプ
     */
    private static final String INITIAL_VOICE_TYPE = null;

    /**
     * 初期状態で拒否されているかどうか
     */
    private static final boolean INITIAL_DENY = false;

    /**
     * 初期状態のニックネーム
     */
    private static final String INITIAL_NICK_NAME = null;

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * ユーザーID
     */
    private final long userId;

    ServerUserDataImpl(DataRepositoryImpl repository, long serverId, long userId) {
        super(repository);
        this.serverId = serverId;
        this.userId = userId;
    }

    @Override
    protected RecordData<ServerUserKey, ServerUserDataRecord>.InitRecordContext getInitRecordContext() {
        int serverKeyId = repository.getServerKeyData().getId(serverId);
        int userKeyId = repository.getUserKeyData().getId(userId);
        ServerUserKey serverUserKey = new ServerUserKey(serverKeyId, userKeyId);

        return new InitRecordContext(dao().serverUserDataTable(), serverUserKey, () -> {
            OptionalInt voiceTypeKeyId = repository.getVoiceTypeKeyData().getIdNullable(INITIAL_VOICE_TYPE);
            return new ServerUserDataRecord(
                    voiceTypeKeyId.isPresent() ? voiceTypeKeyId.getAsInt() : null,
                    INITIAL_DENY,
                    INITIAL_NICK_NAME
            );
        });
    }

    @Override
    public @Nullable String getVoiceType() {
        return sqlProcReturnable(con -> {
            OptionalInt voiceType = dao().serverUserDataTable().selectVoiceType(con, recordId());
            if (voiceType.isPresent()) {
                return repository.getVoiceTypeKeyData().getKey(voiceType.getAsInt());
            } else {
                return null;
            }
        });
    }

    @Override
    public void setVoiceType(@Nullable String voiceType) {
        sqlProc(con -> {
            OptionalInt voiceTypeKeyId = repository.getVoiceTypeKeyData().getIdNullable(voiceType);
            dao().serverUserDataTable().updateVoiceType(con, recordId(), voiceTypeKeyId.isPresent() ? voiceTypeKeyId.getAsInt() : null);
        });
    }

    @Override
    public boolean isDeny() {
        return sqlProcReturnable(con ->
                dao().serverUserDataTable().selectDeny(con, recordId())
        );
    }

    @Override
    public void setDeny(boolean deny) {
        sqlProc(con -> dao().serverUserDataTable().updateDeny(con, recordId(), deny));
    }

    @Override
    public @Nullable String getNickName() {
        return sqlProcReturnable(con ->
                dao().serverUserDataTable().selectNickName(con, recordId()).orElse(null)
        );
    }

    @Override
    public void setNickName(@Nullable String nickName) {
        sqlProc(con -> dao().serverUserDataTable().updateNickName(con, recordId(), nickName));
    }
}
