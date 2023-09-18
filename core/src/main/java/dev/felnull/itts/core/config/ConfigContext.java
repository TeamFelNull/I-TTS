package dev.felnull.itts.core.config;


import org.jetbrains.annotations.Nullable;

/**
 * コンフィグのコンテキスト
 *
 * @author MORIMORI0317
 */
public interface ConfigContext {

    /**
     * コンフィグを取得
     *
     * @return コンフィグ
     */
    @Nullable
    Config loadConfig();
}
