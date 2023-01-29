package dev.felnull.ttsvoice.core.voice;

import com.google.common.hash.Hashing;
import dev.felnull.ttsvoice.core.audio.loader.CachedVoiceTrackLoader;
import dev.felnull.ttsvoice.core.audio.loader.VoiceTrackLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class CachedVoiceType implements VoiceType {
    abstract public InputStream openVoiceStream(String text) throws IOException, InterruptedException;

    protected String createHashCodeChars() {
        return getCategory().getId() + "-" + getId();
    }

    @Override
    public VoiceTrackLoader createVoiceTrackLoader(String text) {
        var hash = Hashing.murmur3_128().hashString(text + "-" + createHashCodeChars(), StandardCharsets.UTF_8);
        return new CachedVoiceTrackLoader(hash, () -> openVoiceStream(text));
    }
}
