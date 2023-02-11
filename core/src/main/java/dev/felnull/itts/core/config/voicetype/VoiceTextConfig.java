package dev.felnull.itts.core.config.voicetype;

import org.jetbrains.annotations.NotNull;

/**
 * VoiceTextのコンフィグ
 */
public interface VoiceTextConfig extends VoiceTypeConfig {
    String DEFAULT_API_KEY = "";

    @NotNull
    String getApiKey();
}
