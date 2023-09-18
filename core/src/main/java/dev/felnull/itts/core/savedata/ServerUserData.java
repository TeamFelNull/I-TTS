package dev.felnull.itts.core.savedata;

import org.jetbrains.annotations.Nullable;

/**
 * サーバーごとのユーザデータ
 *
 * @author MORIMORI0317
 */
public interface ServerUserData {

    /**
     * バージョン
     */
    int VERSION = 0;

    /**
     * 初期状態の音声タイプ
     */
    String INIT_VOICE_TYPE = null;

    /**
     * 初期状態で拒否されているかどうか
     */
    boolean INIT_DENY = false;

    /**
     * 初期状態のニックネーム
     */
    String INIT_NICK_NAME = null;

    /**
     * 音声タイプを取得
     *
     * @return 音声タイプ
     */
    @Nullable
    String getVoiceType();

    /**
     * 音声タイプを変更
     *
     * @param voiceType 音声タイプ
     */
    void setVoiceType(@Nullable String voiceType);

    /**
     * 拒否されているかを取得
     *
     * @return 拒否されているかどうか
     */
    boolean isDeny();

    /**
     * 拒否されているかを変更
     *
     * @param deny 拒否されているかどうか
     */
    void setDeny(boolean deny);

    /**
     * ニックネームを取得
     *
     * @return ニックネーム
     */
    @Nullable
    String getNickName();

    /**
     * ニックネームを変更
     *
     * @param nickName ニックネーム
     */
    void setNickName(@Nullable String nickName);
}
