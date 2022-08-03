package dev.felnull.ttsvoice.voice.reinoare.cookie;

import dev.felnull.ttsvoice.tts.sayedtext.SayedText;
import dev.felnull.ttsvoice.tts.sayedtext.StartupSayedText;
import dev.felnull.ttsvoice.tts.sayedtext.VCEventSayedText;
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
    public String getSayVoiceSoundURL(SayedText sayedText) throws Exception {
        if (sayedText instanceof VCEventSayedText || sayedText instanceof StartupSayedText)
            return null;
        return URLVoiceType.super.getSayVoiceSoundURL(sayedText);
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
    public InputStream getSayVoiceSound(SayedText sayedText) throws Exception {
        var in = CookieManager.getInstance();
        if (sayedText instanceof VCEventSayedText vcEventSayVoice) {
            return switch (vcEventSayVoice.getEventType()) {
                case JOIN -> in.getJoinSound();
                case MOVE_FROM -> in.getMoveFromSound();
                case FORCE_MOVE_FROM -> in.getForceMoveFromSound();
                case LEAVE -> in.getLeaveSound();
                case FORCE_LEAVE -> in.getForceLeaveSound();
                case MOVE_TO -> in.getMoveToSound();
                case FORCE_MOVE_TO -> in.getForceMoveToSound();
            };
        } else if (sayedText instanceof StartupSayedText) {
            return in.getJoinSound();
        }
        return URLVoiceType.super.getSayVoiceSound(sayedText);
    }

    @Override
    public boolean isCached(SayedText sayVoice) {
        return false;
    }
}
