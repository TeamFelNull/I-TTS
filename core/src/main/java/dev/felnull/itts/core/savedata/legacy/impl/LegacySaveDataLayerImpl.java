package dev.felnull.itts.core.savedata.legacy.impl;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.*;
import dev.felnull.itts.core.savedata.repository.IdCustomDictionaryEntryPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * レガシーセーブデータレイヤーの実装
 */
public final class LegacySaveDataLayerImpl implements LegacySaveDataLayer {

    /**
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

    /**
     * コンストラクタ
     *
     * @param saveDataManager セーブデータマネージャー
     */
    public LegacySaveDataLayerImpl(SaveDataManager saveDataManager) {
        this.saveDataManager = saveDataManager;
    }

    @Override
    public @NotNull LegacyServerData getServerData(long guildId) {
        return new LegacyServerDataImpl(saveDataManager, guildId);
    }

    @Override
    public @NotNull LegacyServerUserData getServerUserData(long guildId, long userId) {
        return new LegacyServerUserDataImpl(saveDataManager, guildId, userId);
    }

    @Override
    public @NotNull @Unmodifiable List<LegacyDictData> getAllServerDictData(long guildId) {
        List<LegacyDictData> allDictData = saveDataManager.getRepository().getServerCustomDictionaryData(guildId).getAll()
                .stream().map(IdCustomDictionaryEntryPair::entry)
                .map(it -> (LegacyDictData) new LegacyDictDataImpl(it.target(), it.read()))
                .toList();
        return ImmutableList.copyOf(allDictData);
    }

    @Override
    public @Nullable LegacyDictData getServerDictData(long guildId, @NotNull String target) {
        IdCustomDictionaryEntryPair ret = saveDataManager.getRepository().getServerCustomDictionaryData(guildId).getByTarget(target).stream().findFirst().orElse(null);
        if (ret != null) {
            return new LegacyDictDataImpl(ret.entry().target(), ret.entry().read());
        }
        return null;
    }

    @Override
    public void addServerDictData(long guildId, @NotNull String target, @NotNull String read) {
        saveDataManager.getRepository().getServerCustomDictionaryData(guildId).add(new CustomDictionaryEntry(target, read, ReplaceType.WORD));
    }

    @Override
    public void removeServerDictData(long guildId, @NotNull String target) {
        @NotNull @Unmodifiable List<IdCustomDictionaryEntryPair> ret = saveDataManager.getRepository().getServerCustomDictionaryData(guildId).getByTarget(target);
        ret.forEach(it -> saveDataManager.getRepository().getServerCustomDictionaryData(guildId).remove(it.id()));
    }

    @Override
    public @NotNull @Unmodifiable List<LegacyDictData> getAllGlobalDictData() {
        List<LegacyDictData> allDictData = saveDataManager.getRepository().getGlobalCustomDictionaryData().getAll()
                .stream().map(IdCustomDictionaryEntryPair::entry)
                .map(it -> (LegacyDictData) new LegacyDictDataImpl(it.target(), it.read()))
                .toList();
        return ImmutableList.copyOf(allDictData);
    }

    @Override
    public @NotNull @Unmodifiable List<Long> getAllDenyUser(long guildId) {
        return saveDataManager.getRepository().getAllDenyUser(guildId);
    }
}
