package dev.felnull.itts.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Discordに音声を送るためのハンドラー
 *
 * @author MORIMORI0317
 */
public class VoiceAudioHandler implements AudioSendHandler {
    /**
     * オーディオプレーヤー
     */
    private final AudioPlayer audioPlayer;

    /**
     * オーディオバッファー
     */
    private final ByteBuffer buffer;

    /**
     * オーディオフレーム
     */
    private final MutableAudioFrame frame;

    /**
     * コンストラクタ
     *
     * @param audioPlayer オーディオプレーヤー
     */
    public VoiceAudioHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.provide(frame);
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        buffer.flip();
        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
