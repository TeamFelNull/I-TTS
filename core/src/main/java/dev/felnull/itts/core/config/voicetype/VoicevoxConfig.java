package dev.felnull.itts.core.config.voicetype;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * VOICEVOX系共通コンフィグ
 */
public interface VoicevoxConfig extends VoiceTypeConfig {
    List<String> DEFAULT_API_URLS = ImmutableList.of("");
    long DEFAULT_CHECK_TIME = 15000;

    @NotNull
    @Unmodifiable
    List<String> getApiUrls();

    long getCheckTime();
}
