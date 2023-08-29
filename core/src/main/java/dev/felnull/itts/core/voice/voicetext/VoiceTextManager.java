package dev.felnull.itts.core.voice.voicetext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.voice.VoiceType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class VoiceTextManager implements ITTSRuntimeUse {
    private static final String API_URL = "https://api.voicetext.jp/v1/tts";
    private static final Gson GSON = new Gson();
    private final VoiceTextVoiceCategory category = new VoiceTextVoiceCategory();
    private final List<VoiceType> voiceTypes = Arrays.stream(VoiceTextSpeaker.values()).map(VoiceTextVoiceType::new).map(t -> (VoiceType) t).toList();

    public VoiceTextVoiceCategory getCategory() {
        return category;
    }

    public List<VoiceType> getVoiceTypes() {
        return voiceTypes;
    }

    private String getApiKey() {
        return getConfigManager().getConfig().getVoiceTextConfig().getApiKey();
    }

    public boolean isAvailable() {
        return getConfigManager().getConfig().getVoiceTextConfig().isEnable();
    }

    public InputStream openVoiceStream(@NotNull VoiceTextSpeaker speaker, @NotNull String text) throws IOException, InterruptedException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpClient hc = getNetworkManager().getHttpClient();
        var basic = "Basic " + FNStringUtil.encodeBase64(getApiKey() + ":");
        var request = HttpRequest.newBuilder(URI.create(API_URL))
                .header("Authorization", basic)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(String.format("text=%s&speaker=%s", text, speaker.getId())))
                .build();
        var res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());

        var content = res.headers().firstValue("content-type");
        int code = res.statusCode();

        if (content.isEmpty())
            throw new IOException("Content Type does not exist: " + code);

        if (content.get().startsWith("audio/"))
            return res.body();

        if ("application/json".equals(content.get())) {
            try (InputStream stream = new BufferedInputStream(res.body()); Reader reader = new InputStreamReader(stream)) {
                var jo = GSON.fromJson(reader, JsonObject.class);
                var ejo = jo.getAsJsonObject("error");
                throw new IOException("VoiceText error (" + ejo.get("message").getAsString() + "): " + code);
            } catch (JsonSyntaxException ignored) {
            }
        }

        throw new IOException("Not audio data: " + code);
    }
}
