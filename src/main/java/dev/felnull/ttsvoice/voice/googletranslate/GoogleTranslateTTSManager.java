package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.SimpleAliveChecker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleTranslateTTSManager {
    private static final GoogleTranslateTTSManager INSTANCE = new GoogleTranslateTTSManager();
    private static final String TTS_URL = "https://translate.google.com.vn/translate_tts?ie=UTF-8&q=%s&tl=%s&client=tw-ob";
    public static final SimpleAliveChecker ALIVE_CHECKER = new SimpleAliveChecker(() -> Main.getConfig().voiceConfig().enableGoogleTranslateTts(), () -> {
        try {
            try (var stream = getInstance().getVoice("ikisugi", "ja")) {
                return stream.readAllBytes().length > 0;
            }
        } catch (Exception ex) {
            return false;
        }
    });

    public static GoogleTranslateTTSManager getInstance() {
        return INSTANCE;
    }

    public InputStream getVoice(String text, String lang) throws IOException, URISyntaxException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var url = String.format(TTS_URL, text, lang);
        var con = FNURLUtil.getConnection(new URL(url));
        con.connect();

        var header = con.getHeaderField("content-type");
        if (header != null && header.startsWith("audio/"))
            return con.getInputStream();

        return null;
    }

    public boolean isAlive() {
        return ALIVE_CHECKER.isAlive();
    }
}
