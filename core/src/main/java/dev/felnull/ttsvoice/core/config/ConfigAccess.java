package dev.felnull.ttsvoice.core.config;


import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import org.jetbrains.annotations.Nullable;

public interface ConfigAccess {
    @Nullable
    Config loadConfig(TTSVoiceRuntime runtime);
}
