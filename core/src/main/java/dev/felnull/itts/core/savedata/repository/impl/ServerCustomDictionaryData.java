package dev.felnull.itts.core.savedata.repository.impl;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.dao.DictionaryRecord;
import dev.felnull.itts.core.savedata.dao.ServerKey;
import dev.felnull.itts.core.savedata.repository.CustomDictionaryData;
import dev.felnull.itts.core.savedata.repository.IdCustomDictionaryEntryPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

/**
 * サーバーカスタム辞書データの実装
 */
class ServerCustomDictionaryData extends SaveDataBase implements CustomDictionaryData {

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * サーバーキー
     */
    private ServerKey serverKey;

    ServerCustomDictionaryData(DataRepositoryImpl repository, long serverId) {
        super(repository);
        this.serverId = serverId;
    }

    void init() {
        serverKey = new ServerKey(repository.getServerKeyData().getId(serverId));
    }

    @Override
    public @NotNull @Unmodifiable List<IdCustomDictionaryEntryPair> getAll() {
        return sqlProcReturnable(connection -> {
            Map<Integer, DictionaryRecord> dictData = dao().serverCustomDictionaryTable().selectRecords(connection, serverKey);

            ImmutableList.Builder<IdCustomDictionaryEntryPair> retBuilder = ImmutableList.builder();

            dictData.forEach((key, entry) -> {
                ReplaceType replaceType =
                        ReplaceType.getByName(repository.getDictionaryReplaceTypeKeyData().getKey(entry.replaceTypeKeyId())).orElse(ReplaceType.CHARACTER);
                retBuilder.add(new IdCustomDictionaryEntryPair(key, new CustomDictionaryEntry(entry.target(), entry.read(), replaceType)));
            });

            return retBuilder.build();
        });
    }

    @Override
    public @NotNull @Unmodifiable List<IdCustomDictionaryEntryPair> getByTarget(@NotNull String target) {
        return sqlProcReturnable(connection -> {
            Map<Integer, DictionaryRecord> dictData = dao().serverCustomDictionaryTable().selectRecordByTarget(connection, serverKey, target);

            ImmutableList.Builder<IdCustomDictionaryEntryPair> retBuilder = ImmutableList.builder();
            dictData.forEach((key, entry) -> {
                ReplaceType replaceType =
                        ReplaceType.getByName(repository.getDictionaryReplaceTypeKeyData().getKey(entry.replaceTypeKeyId())).orElse(ReplaceType.CHARACTER);
                retBuilder.add(new IdCustomDictionaryEntryPair(key, new CustomDictionaryEntry(entry.target(), entry.read(), replaceType)));
            });

            return retBuilder.build();
        });
    }

    @Override
    public void add(@NotNull CustomDictionaryEntry dictionaryEntry) {
        sqlProc(connection -> {
            int replaceTypeKeyId = repository.getDictionaryReplaceTypeKeyData().getId(dictionaryEntry.replaceType().getName());
            DictionaryRecord record = new DictionaryRecord(dictionaryEntry.target(), dictionaryEntry.read(), replaceTypeKeyId);
            dao().serverCustomDictionaryTable().insertRecord(connection, serverKey, record);
        });
    }

    @Override
    public void remove(int entryId) {
        sqlProc(connection -> dao().serverCustomDictionaryTable().deleteRecord(connection, entryId));
    }
}
