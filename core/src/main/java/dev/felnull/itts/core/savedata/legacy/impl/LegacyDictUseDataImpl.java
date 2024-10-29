package dev.felnull.itts.core.savedata.legacy.impl;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictUseData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * レガシー辞書使用データの実装
 */
final class LegacyDictUseDataImpl implements LegacyDictUseData {

    /**
     * セーブデータマネージャー
     */
    private final SaveDataManager saveDataManager;

    /**
     * サーバーID
     */
    private final long serverId;

    /**
     * 辞書Id
     */
    private final String dictionaryId;

    /**
     * コンストラクタ
     *
     * @param saveDataManager セーブデータマネージャー
     * @param serverId        サーバーID
     * @param dictionaryId    辞書Id
     */
    LegacyDictUseDataImpl(SaveDataManager saveDataManager, long serverId, String dictionaryId) {
        this.saveDataManager = saveDataManager;
        this.serverId = serverId;
        this.dictionaryId = dictionaryId;
    }

    private static int initPriority(String dictId) {
        Optional<Pair<String, Integer>> def = ITTSRuntime.getInstance().getDictionaryManager().getDefault().stream()
                .filter(it -> it.getKey().equals(dictId))
                .findFirst();

        return def.map(Pair::getRight).orElse(-1);
    }

    @Override
    public @NotNull String getDictId() {
        return dictionaryId;
    }

    @Override
    public int getPriority() {
        Boolean enable = saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).isEnable();
        Integer priory = saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).getPriority();

        if (enable == null) {
            return initPriority(dictionaryId);
        } else if (Boolean.TRUE.equals(enable)) {
            return priory == null ? initPriority(dictionaryId) : priory;
        } else {
            return -1;
        }
    }

    @Override
    public void setPriority(int priority) {
        if (priority >= 0) {
            saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).setEnable(true);
            saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).setPriority(priority);
        } else {
            saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).setEnable(false);
            saveDataManager.getRepository().getDictionaryUseData(serverId, dictionaryId).setPriority(-1);
        }
    }
}
