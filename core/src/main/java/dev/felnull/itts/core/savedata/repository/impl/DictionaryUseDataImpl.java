package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.DictionaryUseDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerDictionaryKey;
import dev.felnull.itts.core.savedata.repository.DictionaryUseData;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * 辞書使用データの実装
 */
public class DictionaryUseDataImpl extends RecordData<ServerDictionaryKey, DictionaryUseDataRecord> implements DictionaryUseData {

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * 辞書ID
     */
    private final String dictionaryId;


    DictionaryUseDataImpl(DataRepositoryImpl repository, long serverId, String dictionaryId) {
        super(repository);
        this.serverId = serverId;
        this.dictionaryId = dictionaryId;
    }

    @Override
    protected RecordData<ServerDictionaryKey, DictionaryUseDataRecord>.InitRecordContext getInitRecordContext() {
        int serverKeyId = repository.getServerKeyData().getId(serverId);
        int dictionaryKeyId = repository.getDictionaryKeyData().getId(dictionaryId);
        ServerDictionaryKey serverDictionaryKey = new ServerDictionaryKey(serverKeyId, dictionaryKeyId);

        return new InitRecordContext(dao().dictionaryUseDataTable(), serverDictionaryKey,
                () -> new DictionaryUseDataRecord(null, null));
    }

    @Override
    public @Nullable Boolean isEnable() {
        return sqlProcReturnable(con ->
                dao().dictionaryUseDataTable().selectEnable(con, recordId()).orElse(null)
        );
    }

    @Override
    public void setEnable(@Nullable Boolean enable) {
        sqlProc(con -> dao().dictionaryUseDataTable().updateEnable(con, recordId(), enable));
    }

    @Override
    public @Nullable Integer getPriority() {
        return sqlProcReturnable(con -> {
            OptionalInt priority = dao().dictionaryUseDataTable().selectPriority(con, recordId());
            return priority.isPresent() ? priority.getAsInt() : null;
        });
    }

    @Override
    public void setPriority(@Nullable Integer priority) {
        sqlProc(con -> dao().dictionaryUseDataTable().updatePriority(con, recordId(), priority));
    }
}
