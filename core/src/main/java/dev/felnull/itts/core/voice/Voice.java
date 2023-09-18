package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;

/**
 * 声
 *
 * @author MORIMORI0317
 */
public interface Voice {

    /**
     * 利用可能かどうかを取得
     *
     * @return 利用可能かどうか
     */
    boolean isAvailable();

    /**
     * 声トラックローダーを作成
     *
     * @param text テキスト
     * @return 声トラックローダー
     */
    VoiceTrackLoader createVoiceTrackLoader(String text);

    /**
     * 声タイプを取得
     *
     * @return 声タイプ
     */
    VoiceType getVoiceType();

    /**
     * 最大読み上げ文字数
     *
     * @return 文字数
     */
    default int getReadLimit() {
        return Integer.MAX_VALUE;
    }
}
