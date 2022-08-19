package dev.felnull.ttsvoice.voice.voicetext;

import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.SimpleAliveChecker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class VoiceTextManager {
    private static final VoiceTextManager INSTANCE = new VoiceTextManager();
    private static final String API_URL = "https://api.voicetext.jp/v1/tts";
    public static final SimpleAliveChecker ALIVE_CHECKER = new SimpleAliveChecker(() -> Main.getConfig().voiceConfig().enableVoiceText(), () -> {
        try {
            try (var stream = new BufferedInputStream(getInstance().getVoice("ikisugi", VTVoiceTypes.BEAR))) {
                return stream.readAllBytes().length > 0;
            }
        } catch (Exception ex) {
            return false;
        }
    });

    public static VoiceTextManager getInstance() {
        return INSTANCE;
    }

    public String getAPIKey() {
        return Main.getConfig().voiceTextAPIKey();
    }

    public InputStream getVoice(String text, VTVoiceTypes vtVoiceTypes) throws IOException, InterruptedException, URISyntaxException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var hc = HttpClient.newHttpClient();
        String basic = "Basic " + FNStringUtil.encodeBase64(getAPIKey() + ":");
        var request = HttpRequest.newBuilder(URI.create(API_URL)).header("Authorization", basic).header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8").POST(HttpRequest.BodyPublishers.ofString(String.format("text=%s&speaker=%s", text, vtVoiceTypes.getName()))).version(HttpClient.Version.HTTP_1_1).build();
        var res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (res.headers().firstValue("content-type").map(n -> n.startsWith("audio/")).orElse(false))
            return res.body();

        return null;
    }

    public boolean isAlive() {
        return ALIVE_CHECKER.isAlive();
    }
}
