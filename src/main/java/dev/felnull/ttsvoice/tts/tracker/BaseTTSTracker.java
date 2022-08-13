package dev.felnull.ttsvoice.tts.tracker;

import java.util.function.Consumer;

public abstract class BaseTTSTracker {
    private Consumer<TTSTrackerInfo> infoListener;

    abstract public TTSTrackerInfo getUpdateVoice();

    public void onUpdateVoice(TTSTrackerInfo info) {
        if (infoListener != null)
            infoListener.accept(info);
    }

    public void setUpdateVoiceListener(Consumer<TTSTrackerInfo> infoListener) {
        this.infoListener = infoListener;
    }

    abstract public void depose();
}
