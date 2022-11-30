package dev.felnull.ttsvoice.core.config.voicetype;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * VOICEVOX系共通コンフィグ
 */
public class VVEBVoiceTypeConfig extends VoiceTypeConfig {
    protected List<String> apiUrls = ImmutableList.of();

    @NotNull
    @Unmodifiable
    public List<String> getApiUrls() {
        return apiUrls;
    }
}
