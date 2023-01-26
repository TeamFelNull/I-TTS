package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import dev.felnull.ttsvoice.core.audio.VoiceAudioScheduler;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TTSInstance {
    private final ConcurrentLinkedQueue<SaidText> saidTextQueue = new ConcurrentLinkedQueue<>();
    private final VoiceAudioScheduler voiceAudioScheduler;
    private final long audioChannel;
    private final long textChannel;

    public TTSInstance(Guild guild, long audioChannel, long textChannel) {
        this.voiceAudioScheduler = new VoiceAudioScheduler(guild.getAudioManager(), TTSVoiceRuntime.getInstance().getVoiceAudioManager());
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

    public void sayText(SaidText saidText) {
        voiceAudioScheduler.test();
    }
}
