package dev.felnull.ttsvoice.core.audio;

import dev.felnull.ttsvoice.core.tts.saidtext.SaidText;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoadedSaidText {
    private final SaidText saidText;
    private final AtomicBoolean alreadyUsed = new AtomicBoolean();

    public LoadedSaidText(SaidText saidText) {
        this.saidText = saidText;
    }

    public SaidText getSaidText() {
        return saidText;
    }

    public boolean isFailure() {
        return false;
    }

    public void dispose() {
        System.out.println("Dispose: " + saidText);
    }

    public void setAlreadyUsed(boolean alreadyUsed) {
        this.alreadyUsed.set(alreadyUsed);
    }

    public boolean isAlreadyUsed() {
        return alreadyUsed.get();
    }
}
