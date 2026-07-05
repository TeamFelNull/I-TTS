package dev.felnull.itts.core.statistics.repository;

/**
 * 統計レポジトリのエラーリスナー
 */
@FunctionalInterface
public interface StatisticsRepoErrorListener {

    /**
     * エラー発生時に呼び出される
     *
     * @param throwable 発生したエラー
     */
    void onError(Throwable throwable);
}
