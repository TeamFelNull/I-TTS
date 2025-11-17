package dev.felnull.itts.core.savedata.dao;


import org.jetbrains.annotations.Nullable;

/**
 * BOT状態データのレコード
 *
 * @param speakAudioChannelKey          接続オーディオチャンネル
 * @param readTextChannelKey            読み上げるテキストチャンネル
 * @param reconnectSpeakAudioChannelKey 再接続先オーディオチャンネル
 * @param reconnectReadTextChannelKey   再接続先読み上げチャンネル
 */
public record BotStateDataRecord(@Nullable Integer speakAudioChannelKey,
                                 @Nullable Integer readTextChannelKey,
                                 @Nullable Integer reconnectSpeakAudioChannelKey,
                                 @Nullable Integer reconnectReadTextChannelKey
) {
}
