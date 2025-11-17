package dev.felnull.itts.core.savedata.repository.impl;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.dao.DictionaryRecord;
import dev.felnull.itts.core.savedata.repository.CustomDictionaryData;
import dev.felnull.itts.core.savedata.repository.IdCustomDictionaryEntryPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

/**
 * 共通カスタム辞書データの実装
 */
class GlobalCustomDictionaryData extends SaveDataBase implements CustomDictionaryData {

    GlobalCustomDictionaryData(DataRepositoryImpl repository) {
        super(repository);
    }

    @Override
    public @NotNull @Unmodifiable List<IdCustomDictionaryEntryPair> getAll() {
        return sqlProcReturnable(connection -> {
            Map<Integer, DictionaryRecord> dictData = dao().globalCustomDictionaryTable().selectRecords(connection);

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
            Map<Integer, DictionaryRecord> dictData = dao().globalCustomDictionaryTable().selectRecordByTarget(connection, target);

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
            dao().globalCustomDictionaryTable().insertRecord(connection, record);
        });
    }

    @Override
    public void remove(int entryId) {
        sqlProc(connection -> dao().globalCustomDictionaryTable().deleteRecord(connection, entryId));
    }
}
