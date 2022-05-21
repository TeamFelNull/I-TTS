package dev.felnull.ttsvoice.voice.voicevox;

import dev.felnull.ttsvoice.tts.IVoiceType;
import dev.felnull.ttsvoice.util.TextUtil;

public record VVVoiceType(int vvId, String name, String styleName) implements IVoiceType {

    @Override
    public String getTitle() {
        return name + "(" + styleName + ")";
    }

    @Override
    public String getId() {
        return "voicevox-" + vvId;
    }

    @Override
    public byte[] getSound(String text) throws Exception {
        var vvm = VoiceVoxManager.getInstance();
        var q = vvm.getQuery(text);
        return vvm.getVoce(q, vvId);
    }

    @Override
    public String replace(String text) {
        return TextUtil.replaceLatinToHiragana(text);
    }
}
