package dev.felnull.ttsvoice.voicevox;

import dev.felnull.ttsvoice.tts.IVoiceType;

public record VVSpeaker(int vvId, String name, String styleName) implements IVoiceType {

    @Override
    public String getTitle() {
        return name + " - " + styleName;
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
}
