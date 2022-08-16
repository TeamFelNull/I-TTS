package dev.felnull.ttsvoice.voice.voicetext;

import com.google.common.base.CaseFormat;
import dev.felnull.fnjl.util.FNMath;
import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.VoiceType;

import java.io.InputStream;

public enum VTVoiceTypes implements VoiceType {
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
    public InputStream getSound(String text) throws Exception {
        return VoiceTextManager.getInstance().getVoice(text, this);
    }

    @Override
    public int getMaxTextLength(long guildId) {
        return FNMath.clamp(VoiceType.super.getMaxTextLength(guildId), 1, 180);
    }

    @Override
    public VoiceCategory getCategory() {
        return VTVoiceCategory.getInstance();
    }

    @Override
    public boolean isAlive() {
        return VoiceTextManager.getInstance().isAlive();
    }
}
