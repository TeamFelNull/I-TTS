package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.ttsvoice.tts.IVoiceType;
import dev.felnull.ttsvoice.util.TextUtils;

import java.io.InputStream;

public enum GoogleTranslateTTSType implements IVoiceType {
    JA("ja", "日本語"),
    EN("en", "英語"),
    ZH("zh", "中国語"),
    KO("ko", "韓国語"),
    RU("ru", "ロシア語"),
    FR("fr", "フランス語"),
    IT("it", "イタリア語");
    private final String lang;
    private final String name;

    GoogleTranslateTTSType(String lang, String name) {
        this.lang = lang;
        this.name = name;
    }

    @Override
    public String getTitle() {
        return "Google翻訳TTS(" + name + ")";
    }

    @Override
    public String getId() {
        return "google-translate-tts-" + lang;
    }

    public String getName() {
        return name;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public InputStream getSound(String text) throws Exception {
        if (text.isEmpty()) return null;
        return GoogleTranslateTTSManager.getInstance().getVoice(text, lang);
    }

    @Override
    public String replace(String text) {
        if (this != JA)
            return TextUtils.replaceJapaneseToLatin(IVoiceType.super.replace(text)).replace("~", "");
        return IVoiceType.super.replace(text);
    }
}
