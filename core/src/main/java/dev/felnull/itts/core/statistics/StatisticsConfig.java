package dev.felnull.itts.core.statistics;

import dev.felnull.itts.core.config.DataBaseConfig;
import org.jetbrains.annotations.NotNull;

/**
 * 統計機能のコンフィグ
 */
public interface StatisticsConfig {

    /**
     * デフォルトの有効状態
     */
    boolean DEFAULT_ENABLE = true;

    /**
     * 統計機能が有効かどうか
     *
     * @return 有効ならtrue
     */
    boolean isEnable();

    /**
     * 統計データベースのコンフィグ
     *
     * @return データベースコンフィグ
     */
    @NotNull
    DataBaseConfig getDataBase();
}
