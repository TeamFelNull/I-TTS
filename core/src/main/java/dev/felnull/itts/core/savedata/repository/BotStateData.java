package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.tts.TTSChannelPair;
import org.jetbrains.annotations.Nullable;

/**
 * BOTの状態データ
 */
public interface BotStateData {

    /**
     * 接続されているチェンネルを取得する
     *
     * @return 接続されているチャンネルのペア
     */
    @Nullable
    TTSChannelPair getConnectedChannelPair();

    /**
     * 接続されているチャンネルを更新する
     *
     * @param connectedChannel 接続されているチャンネルのペア
     */
    void setConnectedChannelPair(@Nullable TTSChannelPair connectedChannel);

    /**
     * 再接続されるチェンネルを取得する
     *
     * @return 再接続されるチャンネルのペア
     */
    @Nullable
    TTSChannelPair getReconnectChannelPair();

    /**
     * 再接続されるチェンネルを更新する
     *
     * @param reconnectChannel 再接続されるチャンネルのペア
     */
    void setReconnectChannelPair(@Nullable TTSChannelPair reconnectChannel);

    /**
     * 接続中のオーディオチャンネルを取得
     *
     * @return チャンネルID
     */
    @Nullable
    Long getSpeakAudioChannel();

    /**
     * 接続中のオーディオチャンネルを設定
     *
     * @param channelId チャンネルID
     */
    void setSpeakAudioChannel(@Nullable Long channelId);


    /**
     * 読み上げるテキストチャンネルを取得
     *
     * @return チャンネルID
     */
    @Nullable
    Long getReadAroundTextChannel();

    /**
     * 読み上げるテキストチャンネルを設定
     *
     * @param channelId チャンネルID
     */
    void setReadAroundTextChannel(@Nullable Long channelId);

}
