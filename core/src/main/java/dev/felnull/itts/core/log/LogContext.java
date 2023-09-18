package dev.felnull.itts.core.log;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * ログのコンテキスト
 *
 * @author MORIMORI0317
 */
public interface LogContext {

    /**
     * ロガーを取得する
     *
     * @return ロガー
     */
    @NotNull
    Logger getLogger();
}
