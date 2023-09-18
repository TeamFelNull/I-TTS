package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.ITTSRuntime;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 辞書使用データ
 *
 * @author MORIMORI0317
 */
public interface DictUseData {
    /**
     * バージョン
     */
    int VERSION = 0;

    /**
     * 初期の優先度
     *
     * @param dictId 辞書ID
     * @return 優先度
     */
    static int initPriority(String dictId) {
        Optional<Pair<String, Integer>> def = ITTSRuntime.getInstance().getDictionaryManager().getDefault().stream()
                .filter(it -> it.getKey().equals(dictId))
                .findFirst();

        return def.map(Pair::getRight).orElse(-1);
    }

    /**
     * 辞書IDを取得
     *
     * @return 辞書ID
     */
    @NotNull
    String getDictId();

    /**
     * 優先度を取得
     *
     * @return 優先度
     */
    int getPriority();

    /**
     * 優先度を変更
     *
     * @param priority 優先度
     */
    void setPriority(int priority);
}
