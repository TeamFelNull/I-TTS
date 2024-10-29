package dev.felnull.itts.core.savedata.legacy;

/**
 * BOTの状態データ
 *
 * @author MORIMORI0317
 */
public interface LegacyBotStateData {

    /**
     * 接続しているオーディオチャンネルを取得
     *
     * @return 接続オーディオチャンネルのサーバーID
     */
    long getConnectedAudioChannel();

    /**
     * 接続しているオーディオチャンネルを変更
     *
     * @param connectedAudioChannel 接続オーディオチャンネルのサーバーID
     */
    void setConnectedAudioChannel(long connectedAudioChannel);

    /**
     * 読み上げチャンネルを取得
     *
     * @return 読み上げチャンネルのサーバーID
     */
    long getReadAroundTextChannel();

    /**
     * 読み上げチャンネルを変更
     *
     * @param readAroundTextChannel 読み上げチャンネルのサーバーID
     */
    void setReadAroundTextChannel(long readAroundTextChannel);

}
