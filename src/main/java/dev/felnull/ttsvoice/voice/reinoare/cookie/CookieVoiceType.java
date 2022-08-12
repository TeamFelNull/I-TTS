package dev.felnull.ttsvoice.voice.reinoare.cookie;

import dev.felnull.ttsvoice.tts.sayedtext.SayedText;
import dev.felnull.ttsvoice.voice.HasSayVoiceMP3Manager;
import dev.felnull.ttsvoice.voice.HasSayVoiceMP3VoiceType;
import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.reinoare.ReinoareVoiceCategory;

public class CookieVoiceType implements HasSayVoiceMP3VoiceType {
    @Override
    public HasSayVoiceMP3Manager getManager() {
        return CookieManager.getInstance();
    }

    @Override
    public String getTitle() {
        return "クッキー☆";
    }

    @Override
    public String getId() {
        return "reinoare-cookie_star";
    }

    @Override
    public String getSoundURL(String text) throws Exception {
        var im = CookieManager.getInstance();
        var ret = im.search(text);
        var most = im.getMost(ret);
        if (most == null)
            return null;
        return most.getURL();
    }

    @Override
    public boolean isCached(SayedText sayVoice) {
        return false;
    }

    @Override
    public VoiceCategory getCategory() {
        return ReinoareVoiceCategory.getInstance();
    }
}
