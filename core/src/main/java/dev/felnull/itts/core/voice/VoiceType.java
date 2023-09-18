package dev.felnull.itts.core.voice;

/**
 * 声タイプ
 *
 * @author MORIMORI0317
 */
public interface VoiceType {

    /**
     * 声タイプの名前を取得
     *
     * @return 声タイプの名前
     */
    String getName();

    /**
     * 声タイプのIDを取得
     *
     * @return 声タイプのID
     */
    String getId();

    /**
     * 利用可能かどうかを取得
     *
     * @return 利用可能かどうか
     */
    boolean isAvailable();

    /**
     * 声カテゴリを取得
     *
     * @return 声カテゴリ
     */
    VoiceCategory getCategory();

    /**
     * 声を作成
     *
     * @param guildId サーバーID
     * @param userId  ユーザーID
     * @return 声
     */
    Voice createVoice(long guildId, long userId);
}
