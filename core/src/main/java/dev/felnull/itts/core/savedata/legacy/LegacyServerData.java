package dev.felnull.itts.core.savedata.legacy;

import org.jetbrains.annotations.Nullable;

/**
 * サーバーデータ
 *
 * @author MORIMORI0317
 */
public interface LegacyServerData {

    /**
     * デフォルトの音声タイプを取得
     *
     * @return デフォルトの音声タイプ
     */
    @Nullable
    String getDefaultVoiceType();

    /**
     * デフォルトの音声タイプを変更
     *
     * @param voiceType デフォルトの音声タイプ
     */
    void setDefaultVoiceType(@Nullable String voiceType);

    /**
     * 無視する正規表現を取得
     *
     * @return 無視する正規表現
     */
    @Nullable
    String getIgnoreRegex();

    /**
     * 無視する正規表現を変更
     *
     * @param ignoreRegex 無視する正規表現
     */
    void setIgnoreRegex(@Nullable String ignoreRegex);

    /**
     * 参加時のみ読み上げるかどうかを取得
     *
     * @return 参加時のみ読み上げるかどうか
     */
    boolean isNeedJoin();

    /**
     * 参加時のみ読み上げるかどうかを変更
     *
     * @param needJoin 参加時のみ読み上げるかどうか
     */
    void setNeedJoin(boolean needJoin);

    /**
     * 読み上げを上書きするかどうかを取得
     *
     * @return 読み上げを上書きするかどうか
     */
    boolean isOverwriteAloud();

    /**
     * 読み上げをを上書きするかどうかを変更
     *
     * @param overwriteAloud 読み上げを上書きするかどうか
     */
    void setOverwriteAloud(boolean overwriteAloud);

    /**
     * 参加時に読み上げるかどうかを取得
     *
     * @return 参加時に読み上げるかどうか
     */
    boolean isNotifyMove();

    /**
     * 参加時に読み上げるかどうかを変更
     *
     * @param notifyMove 参加時に読み上げるかどうか
     */
    void setNotifyMove(boolean notifyMove);

    /**
     * 最大読み上げ数を取得
     *
     * @return 最大読み上げ数
     */
    int getReadLimit();

    /**
     * 最大読み上げ数を変更
     *
     * @param readLimit 最大読み上げ数
     */
    void setReadLimit(int readLimit);

    /**
     * 名前の最大読み上げ数を取得
     *
     * @return 名前の最大読み上げ数
     */
    int getNameReadLimit();

    /**
     * 名前の最大読み上げ数を変更
     *
     * @param nameReadLimit 名前の最大読み上げ数
     */
    void setNameReadLimit(int nameReadLimit);

}
