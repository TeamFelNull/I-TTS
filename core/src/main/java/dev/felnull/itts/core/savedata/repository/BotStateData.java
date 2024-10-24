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
    TTSChannelPair getConnectedChannel();

    /**
     * 接続されているチャンネルを更新する
     *
     * @param connectedChannel 接続されているチャンネルのペア
     */
    void setConnectedChannel(@Nullable TTSChannelPair connectedChannel);

    /**
     * 再接続されるチェンネルを取得する
     *
     * @return 再接続されるチャンネルのペア
     */
    @Nullable
    TTSChannelPair getReconnectChannel();

    /**
     * 再接続されるチェンネルを更新する
     *
     * @param reconnectChannel 再接続されるチャンネルのペア
     */
    void setReconnectChannel(@Nullable TTSChannelPair reconnectChannel);
}
