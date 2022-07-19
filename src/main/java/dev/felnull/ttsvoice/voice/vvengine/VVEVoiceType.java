package dev.felnull.ttsvoice.voice.vvengine;

import dev.felnull.ttsvoice.util.TextUtils;
import dev.felnull.ttsvoice.voice.VoiceType;

import java.io.InputStream;
import java.util.Objects;

public abstract class VVEVoiceType implements VoiceType {
    private final String engineName;
    private final int vveId;
    private final String name;
    private final String styleName;

    public VVEVoiceType(String engineName, int vveId, String name, String styleName) {
        this.engineName = engineName;
        this.vveId = vveId;
        this.name = name;
        this.styleName = styleName;
    }

    public String getName() {
        return name;
    }

    public String getStyleName() {
        return styleName;
    }

    public int getVVEId() {
        return vveId;
    }

    public String getEngineName() {
        return engineName;
    }

    abstract public VVEngineManager getEngineManager();

    @Override
    public String getTitle() {
        return name + "(" + styleName + ")";
    }

    @Override
    public String getId() {
        return engineName + "-" + vveId;
    }

    @Override
    public InputStream getSound(String text) throws Exception {
        var vvm = getEngineManager();
        var q = vvm.getQuery(text);
        return vvm.getVoce(q, vveId);
    }

    @Override
    public String replace(String text) {
        return TextUtils.replaceLatinToHiragana(VoiceType.super.replace(text));
    }

    @Override
    public float getVolume() {
        return 1.5f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VVEVoiceType that = (VVEVoiceType) o;
        return vveId == that.vveId && Objects.equals(engineName, that.engineName) && Objects.equals(name, that.name) && Objects.equals(styleName, that.styleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engineName, vveId, name, styleName);
    }
}
