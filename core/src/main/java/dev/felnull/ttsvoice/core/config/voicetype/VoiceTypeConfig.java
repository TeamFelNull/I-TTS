package dev.felnull.ttsvoice.core.config.voicetype;

public abstract class VoiceTypeConfig {
    protected boolean enable = true;
    protected long cashTime = 180000;
    protected long checkTime = 15000;

    public boolean isEnable() {
        return enable;
    }

    public long getCashTime() {
        return cashTime;
    }

    public long getCheckTime() {
        return checkTime;
    }
}
