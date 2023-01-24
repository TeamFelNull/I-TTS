package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.audio.VoiceAudioScheduler;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TTSInstance {
    private final ConcurrentLinkedQueue<String> sayQueue = new ConcurrentLinkedQueue<>();
    private final VoiceAudioScheduler voiceAudioScheduler;
    private final long audioChannel;
    private final long textChannel;

    public TTSInstance(TTSVoiceRuntime runtime, Guild guild, long audioChannel, long textChannel) {
        this.voiceAudioScheduler = new VoiceAudioScheduler(guild.getAudioManager(), runtime.getVoiceAudioManager());
        this.audioChannel = audioChannel;
        this.textChannel = textChannel;
    }

    public long getAudioChannel() {
        return audioChannel;
    }

    public long getTextChannel() {
        return textChannel;
    }

    public void dispose() {
        voiceAudioScheduler.dispose();
    }

    public void sayChat(long userId, String text) {

    }

    public void sayText(String text) {
        voiceAudioScheduler.test();
    }
}
