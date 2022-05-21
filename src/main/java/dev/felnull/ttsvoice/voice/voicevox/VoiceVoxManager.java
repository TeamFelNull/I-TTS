package dev.felnull.ttsvoice.voice.voicevox;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VoiceVoxManager {
    private static final Logger LOGGER = LogManager.getLogger(VoiceVoxManager.class);
    private static final Gson GSON = new Gson();
    private static final VoiceVoxManager INSTANCE = new VoiceVoxManager();
    private List<VVVoiceType> SPEAKERS;
    private long lastSpeakersLoadTime;

    public static VoiceVoxManager getInstance() {
        return INSTANCE;
    }

    public String getEngineURL() {
        return Main.CONFIG.voiceVoxURL();
    }

    public String getSpeakersURL() {
        return getEngineURL() + "/speakers";
    }

    public List<VVVoiceType> getSpeakers() {
        loadSpeakers();
        return SPEAKERS;
    }

    private synchronized void loadSpeakers() {
        if (System.currentTimeMillis() - lastSpeakersLoadTime < 1000 * 60 * 10)
            return;
        try {
            JsonArray ja;
            try (Reader reader = new InputStreamReader(FNURLUtil.getStream(new URL(getSpeakersURL())))) {
                ja = GSON.fromJson(reader, JsonArray.class);
            }

            ImmutableList.Builder<VVVoiceType> speakers = new ImmutableList.Builder<>();

            for (JsonElement element : ja) {
                var jo = (JsonObject) element;
                var name = jo.get("name").getAsString();
                var styles = jo.getAsJsonArray("styles");
                for (JsonElement style : styles) {
                    var sjo = (JsonObject) style;
                    speakers.add(new VVVoiceType(sjo.get("id").getAsInt(), name, sjo.get("name").getAsString()));
                }
            }

            SPEAKERS = speakers.build();
            //SPEAKERS = SPEAKERS.stream().sorted(Comparator.comparingInt(Speaker::vvId)).toList();

            LOGGER.info("Successful get of voicevox speakers");
            lastSpeakersLoadTime = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.error("Failed to get voicevox speakers", e);
            lastSpeakersLoadTime = System.currentTimeMillis() - 1000 * 60 * 7;
        }
    }

    public JsonObject getQuery(String text) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();
        var ret = FNURLUtil.getResponseByPOST(new URL(String.format(Main.CONFIG.voiceVoxURL() + "/audio_query?text=%s&speaker=2", text)), "", "", "");
        return GSON.fromJson(ret.getKey(), JsonObject.class);
    }

    public byte[] getVoce(JsonObject query, int speakerId) throws IOException, InterruptedException {
        var hc = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(Main.CONFIG.voiceVoxURL() + "/synthesis?speaker=" + speakerId)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(query))).version(HttpClient.Version.HTTP_1_1).build();
        var res = hc.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return res.body();
    }
}
