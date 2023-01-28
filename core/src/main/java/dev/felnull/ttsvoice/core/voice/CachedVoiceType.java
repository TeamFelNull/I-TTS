package dev.felnull.ttsvoice.core.voice;

import com.google.common.hash.Hashing;
import dev.felnull.ttsvoice.core.audio.loader.CachedVoiceTrackLoader;
import dev.felnull.ttsvoice.core.audio.loader.VoiceTrackLoader;

import java.io.IOException;
import java.io.InputStream;

public abstract class CachedVoiceType implements VoiceType {
    abstract public InputStream openVoiceStream(String text) throws IOException, InterruptedException;

    protected String createHashCodeChars() {
        return getCategory().getId() + "-" + getId();
    }

    @Override
    public VoiceTrackLoader createVoiceTrackLoader(String text) {
        var hash = Hashing.murmur3_128().hashUnencodedChars(text + "-" + createHashCodeChars());
        return new CachedVoiceTrackLoader(hash, () -> openVoiceStream(text));
    }
}
