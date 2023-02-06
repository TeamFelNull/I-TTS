package dev.felnull.itts.core.voice;

import com.google.common.hash.Hashing;
import dev.felnull.itts.core.audio.loader.CachedVoiceTrackLoader;
import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class CachedVoice extends BaseVoice {
    protected CachedVoice(VoiceType voiceType) {
        super(voiceType);
    }

    abstract protected InputStream openVoiceStream(String text) throws IOException, InterruptedException;

    abstract protected String createHashCodeChars();

    @Override
    public VoiceTrackLoader createVoiceTrackLoader(String text) {
        var hash = Hashing.murmur3_128().newHasher()
                .putString(voiceType.getId(), StandardCharsets.UTF_8)
                .putString(text, StandardCharsets.UTF_8)
                .putString(createHashCodeChars(), StandardCharsets.UTF_8)
                .hash();

        return new CachedVoiceTrackLoader(hash, () -> {
            if (!isAvailable())
                throw new RuntimeException("Voice is not available");
            return openVoiceStream(text);
        });
    }
}
