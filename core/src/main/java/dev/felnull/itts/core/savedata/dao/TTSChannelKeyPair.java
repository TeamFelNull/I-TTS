package dev.felnull.itts.core.savedata.dao;

/**
 * 接続先オーディオチャンネルと読み上げるテキストチャンネルキーのペア
 *
 * @param speakAudioChannelKey 接続オーディオチャンネル
 * @param readTextChannelKey   読み上げるテキストチャンネル
 */
public record TTSChannelKeyPair(int speakAudioChannelKey, int readTextChannelKey) {
}
