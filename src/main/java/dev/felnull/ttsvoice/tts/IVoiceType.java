package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;

import java.io.InputStream;

public interface IVoiceType {
    String getTitle();

    String getId();

    InputStream getSound(String text) throws Exception;

    default String replace(String text) {
        return text;
    }

    default InputStream getSayVoiceSound(ISayVoice sayVoice) throws Exception {
        return getSound(replace(sayVoice.getSayVoiceText()));
    }

    default int getMaxTextLength(long guildId) {
        return Main.getServerConfig(guildId).getMaxReadAroundCharacterLimit();
    }

    default float getVolume() {
        return 1f;
    }

    default boolean isCached(ISayVoice sayVoice) {
        return true;
    }
}
