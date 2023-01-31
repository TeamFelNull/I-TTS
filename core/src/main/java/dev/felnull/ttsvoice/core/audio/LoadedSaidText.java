package dev.felnull.ttsvoice.core.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoadedSaidText {
    private final SaidText saidText;
    private final AudioTrack track;
    private final Runnable dispose;
    private final AtomicBoolean alreadyUsed = new AtomicBoolean();

    public LoadedSaidText(SaidText saidText, AudioTrack track, Runnable dispose) {
        this.saidText = saidText;
        this.track = track;
        this.dispose = dispose;
    }

    public SaidText getSaidText() {
        return saidText;
    }

    public boolean isFailure() {
        return track == null;
    }

    public void dispose() {
        dispose.run();
    }

    public void setAlreadyUsed(boolean alreadyUsed) {
        this.alreadyUsed.set(alreadyUsed);
    }

    public boolean isAlreadyUsed() {
        return alreadyUsed.get();
    }

    public AudioTrack getTrack() {
        return track;
    }
}
