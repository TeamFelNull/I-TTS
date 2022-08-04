package dev.felnull.ttsvoice.voice;

import dev.felnull.ttsvoice.tts.sayedtext.SayedText;
import dev.felnull.ttsvoice.tts.sayedtext.StartupSayedText;
import dev.felnull.ttsvoice.tts.sayedtext.VCEventSayedText;

import java.io.InputStream;

public interface HasSayVoiceMP3VoiceType extends URLVoiceType {
    HasSayVoiceMP3Manager getManager();

    @Override
    default String getSayVoiceSoundURL(SayedText sayedText) throws Exception {
        if (sayedText instanceof VCEventSayedText || sayedText instanceof StartupSayedText)
            return null;
        return URLVoiceType.super.getSayVoiceSoundURL(sayedText);
    }

    default InputStream getSayVoiceSound(SayedText sayedText) throws Exception {
        var in = getManager();
        if (sayedText instanceof VCEventSayedText vcEventSayVoice) {
            return switch (vcEventSayVoice.getEventType()) {
                case CONNECT -> in.getConnectSound();
                case JOIN -> in.getJoinSound();
                case MOVE_FROM -> in.getMoveFromSound();
                case FORCE_MOVE_FROM -> in.getForceMoveFromSound();
                case LEAVE -> in.getLeaveSound();
                case FORCE_LEAVE -> in.getForceLeaveSound();
                case MOVE_TO -> in.getMoveToSound();
                case FORCE_MOVE_TO -> in.getForceMoveToSound();
            };
        } else if (sayedText instanceof StartupSayedText) {
            return in.getConnectSound();
        }
        return URLVoiceType.super.getSayVoiceSound(sayedText);
    }
}
