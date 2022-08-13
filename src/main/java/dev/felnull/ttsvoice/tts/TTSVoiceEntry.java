package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.tts.tracker.BaseTTSTracker;

import java.util.UUID;

public record TTSVoiceEntry(TTSVoice voice, UUID uuid, BaseTTSTracker tracker) {
    public void trackerDepose() {
        if (tracker != null)
            tracker.depose();
    }
}
