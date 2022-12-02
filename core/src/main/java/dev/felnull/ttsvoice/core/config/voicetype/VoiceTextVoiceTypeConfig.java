package dev.felnull.ttsvoice.core.config.voicetype;

import org.jetbrains.annotations.NotNull;

/**
 * VoiceTextのコンフィグ
 */
public interface VoiceTextVoiceTypeConfig extends VoiceTypeConfig {
    String DEFAULT_API_KEY = "";

    @NotNull
    String getApiKey();
}
