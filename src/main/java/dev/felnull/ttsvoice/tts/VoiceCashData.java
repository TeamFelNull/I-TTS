package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.Main;

import java.util.UUID;

public class VoiceCashData {
    private final UUID id;
    private long lastTime;

    public VoiceCashData(UUID id) {
        this.id = id;
        lastTime = System.currentTimeMillis();
    }

    public void update() {
        lastTime = System.currentTimeMillis();
    }

    public boolean timeOut() {
        return System.currentTimeMillis() - lastTime >= (long) Main.CONFIG.cashTime() * 60L * 1000L;
    }

    public UUID getId() {
        return id;
    }

    public boolean isInvalid() {
        return id == null;
    }
}
