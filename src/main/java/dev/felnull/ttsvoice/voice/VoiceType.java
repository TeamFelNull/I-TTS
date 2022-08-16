package dev.felnull.ttsvoice.voice;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.tts.sayedtext.SayedText;

import java.io.InputStream;

public interface VoiceType extends HasTitleAndID {

    InputStream getSound(String text) throws Exception;

    default String replace(String text) {
        return text;
    }

    default InputStream getSayVoiceSound(SayedText sayVoice) throws Exception {
        return getSound(toSayVoiceText(sayVoice));
    }

    default String toSayVoiceText(SayedText sayVoice) {
        return replace(sayVoice.getSayVoiceText());
    }

    default int getMaxTextLength(long guildId) {
        return Main.getServerSaveData(guildId).getMaxReadAroundCharacterLimit();
    }

    default float getVolume() {
        return 1f;
    }

    default boolean isCached(SayedText sayVoice) {
        return true;
    }

    VoiceCategory getCategory();

    boolean isAlive();
}
