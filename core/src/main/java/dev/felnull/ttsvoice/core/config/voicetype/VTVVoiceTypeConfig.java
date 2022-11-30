package dev.felnull.ttsvoice.core.config.voicetype;

import org.jetbrains.annotations.NotNull;

/**
 * VoiceTextのコンフィグ
 */
public class VTVVoiceTypeConfig extends VoiceTypeConfig {
    protected String apiKey = "";

    @NotNull
    public String getApiKey() {
        return apiKey;
    }
}
