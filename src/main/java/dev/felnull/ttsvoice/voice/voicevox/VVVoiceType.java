package dev.felnull.ttsvoice.voice.voicevox;

import dev.felnull.ttsvoice.util.TextUtils;
import dev.felnull.ttsvoice.voice.VoiceType;

import java.io.InputStream;

public record VVVoiceType(int vvId, String name, String styleName) implements VoiceType {

    @Override
    public String getTitle() {
        return name + "(" + styleName + ")";
    }

    @Override
    public String getId() {
        return "voicevox-" + vvId;
    }

    @Override
    public InputStream getSound(String text) throws Exception {
        var vvm = VoiceVoxManager.getInstance();
        var q = vvm.getQuery(text);
        return vvm.getVoce(q, vvId);
    }

    @Override
    public String replace(String text) {
        return TextUtils.replaceLatinToHiragana(VoiceType.super.replace(text));
    }

    @Override
    public float getVolume() {
        return 1.5f;
    }
}
