package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.savedata.dao.TTSCountRecord;

import java.util.Optional;

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
     * 該当日のレコードを取得
     *
     * @return レコード 存在しない場合は空
     */
    Optional<TTSCountRecord> getRecord();

    /**
     * 文字数を取得
     *
     * @return 文字数
     */
    default long getCharCount() {
        return getRecord().map(TTSCountRecord::charCount).orElse(0L);
    }

    /**
     * メッセージ数を取得
     *
     * @return メッセージ数
     */
    default long getMessageCount() {
        return getRecord().map(TTSCountRecord::messageCount).orElse(0L);
    }
}
