package dev.felnull.itts.core.savedata.repository;

/**
 * 読み上げ文字数集計データ
 */
public interface TTSCountData {

    /**
     * カウントを増分する
     *
     * @param charDelta    文字数の増分
     * @param messageDelta メッセージ数の増分
     */
    void addCount(long charDelta, long messageDelta);

    /**
     * 文字数を取得
     *
     * @return 文字数
     */
    long getCharCount();

    /**
     * メッセージ数を取得
     *
     * @return メッセージ数
     */
    long getMessageCount();
}
