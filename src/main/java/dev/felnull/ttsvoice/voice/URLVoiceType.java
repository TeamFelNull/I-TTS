package dev.felnull.ttsvoice.voice;

import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;

import java.io.InputStream;
import java.net.URL;

public interface URLVoiceType extends VoiceType {
    String getSoundURL(String text) throws Exception;

    default String getSayVoiceSoundURL(ISayVoice sayVoice) throws Exception {
        return getSoundURL(toSayVoiceText(sayVoice));
    }

    @Override
    default InputStream getSound(String text) throws Exception {
        var url = getSoundURL(text);
        if (url == null) return null;
        return FNURLUtil.getStream(new URL(url));
    }

    @Override
    default InputStream getSayVoiceSound(ISayVoice sayVoice) throws Exception {
        var url = getSayVoiceSoundURL(sayVoice);
        if (url == null) return null;
        return FNURLUtil.getStream(new URL(url));
    }
}
