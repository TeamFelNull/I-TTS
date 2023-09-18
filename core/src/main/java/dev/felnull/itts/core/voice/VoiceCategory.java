package dev.felnull.itts.core.voice;

/**
 * 声カテゴリ
 *
 * @author MORIMORI0317
 */
public interface VoiceCategory {

    /**
     * カテゴリ名を取得
     *
     * @return カテゴリ名
     */
    String getName();

    /**
     * 声IDを取得
     *
     * @return 声ID
     */
    String getId();

    /**
     * 利用可能かどうかを取得
     *
     * @return 利用可能かどうか
     */
    boolean isAvailable();
}
