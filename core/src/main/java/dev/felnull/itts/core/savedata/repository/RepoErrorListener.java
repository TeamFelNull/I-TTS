package dev.felnull.itts.core.savedata.repository;

/**
 * レポジトリの処理でエラーが起きたことを伝えるためのイベントリスナー
 */
public interface RepoErrorListener {

    /**
     * エラー発生したときに呼び出し
     *
     * @param throwable 発生した例外
     */
    void onError(Throwable throwable);
}
