package dev.felnull.itts.core.tts;

/**
 * 接続先オーディオチャンネルと読み上げるテキストチャンネルのペア
 *
 * @param speakAudioChannel 接続オーディオチャンネル
 * @param readTextChannel   読み上げるテキストチャンネル
 */
public record TTSChannelPair(long speakAudioChannel, long readTextChannel) {
}
