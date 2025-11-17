package dev.felnull.itts.core.savedata.repository;

import org.jetbrains.annotations.Nullable;

/**
 * サーバー別ユーザデータ
 */
public interface ServerUserData {

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
