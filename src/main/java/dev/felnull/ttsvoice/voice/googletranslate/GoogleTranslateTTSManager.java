package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.fnjl.util.FNURLUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleTranslateTTSManager {
    private static final GoogleTranslateTTSManager INSTANCE = new GoogleTranslateTTSManager();
    private static final String TTS_URL = "https://translate.google.com.vn/translate_tts?ie=UTF-8&q=%s&tl=%s&client=tw-ob";

    public static GoogleTranslateTTSManager getInstance() {
        return INSTANCE;
    }

    public byte[] getVoice(String text, String lang) throws IOException, InterruptedException, URISyntaxException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var url = String.format(TTS_URL, text, lang);
        try (var st = FNURLUtil.getStream(new URL(url))) {
            return st.readAllBytes();
        }
    }
}
