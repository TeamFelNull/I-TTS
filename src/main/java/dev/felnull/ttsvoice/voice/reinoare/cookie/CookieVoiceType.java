package dev.felnull.ttsvoice.voice.reinoare.cookie;

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
        return "reinoare-cookie_star";
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
            return switch (vcEventSayVoice.getEventType()) {
                case JOIN -> in.getJoinSound();
                case MOVE_FROM -> in.getMoveFromSound();
                case FORCE_MOVE_FROM -> in.getForceMoveFromSound();
                case LEAVE -> in.getLeaveSound();
                case FORCE_LEAVE -> in.getForceLeaveSound();
                case MOVE_TO -> in.getMoveToSound();
                case FORCE_MOVE_TO -> in.getForceMoveToSound();
            };
        }
        return URLVoiceType.super.getSayVoiceSound(sayVoice);
    }

    @Override
    public boolean isCached(ISayVoice sayVoice) {
        return false;
    }
}
