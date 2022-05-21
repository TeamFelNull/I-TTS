package dev.felnull.ttsvoice.voice.voicetext;

import com.google.common.base.CaseFormat;
import dev.felnull.ttsvoice.tts.IVoiceType;

public enum VTVoiceTypes implements IVoiceType {
    SHOW("show", "男性"),
    HARUKA("haruka", "女性"),
    HIKARI("hikari", "女性"),
    TAKERU("takeru", "男性"),
    SANTA("santa", "サンタ"),
    BEAR("bear", "凶暴なクマ");
    private final String name;
    private final String description;

    VTVoiceTypes(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getTitle() {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, getName()) + "(" + getDescription() + ")";
    }

    @Override
    public String getId() {
        return "voicetext-" + getName();
    }

    @Override
    public byte[] getSound(String text) throws Exception {
        return VoiceTextManager.getInstance().getVoice(text, this);
    }
}
