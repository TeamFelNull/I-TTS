package dev.felnull.ttsvoice.voice.cookie;

import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;
import dev.felnull.ttsvoice.tts.sayvoice.VCEventSayVoice;
import dev.felnull.ttsvoice.voice.URLVoiceType;

import java.io.InputStream;

public class CookieVoiceType implements URLVoiceType {
    @Override
    public String getTitle() {
        return "クッキー☆";
    }

    @Override
    public String getId() {
        return "cookie_star";
    }

    @Override
    public String getSayVoiceSoundURL(ISayVoice sayVoice) throws Exception {
        if (sayVoice instanceof VCEventSayVoice)
            return null;
        return URLVoiceType.super.getSayVoiceSoundURL(sayVoice);
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
    public InputStream getSayVoiceSound(ISayVoice sayVoice) throws Exception {
        if (sayVoice instanceof VCEventSayVoice vcEventSayVoice) {
            var in = CookieManager.getInstance();
            if (vcEventSayVoice.getEventType() == VCEventSayVoice.EventType.JOIN || vcEventSayVoice.getEventType() == VCEventSayVoice.EventType.MOVE_FROM)
                return in.getJoinSound();
            if (vcEventSayVoice.getEventType() == VCEventSayVoice.EventType.LEAVE || vcEventSayVoice.getEventType() == VCEventSayVoice.EventType.MOVE_TO)
                return in.getLeaveSound();
        }
        return URLVoiceType.super.getSayVoiceSound(sayVoice);
    }

    @Override
    public boolean isCached(ISayVoice sayVoice) {
        return false;
    }
}
