package dev.felnull.ttsvoice.voice.voicevox;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.fnjl.util.FNURLUtil;
import dev.felnull.ttsvoice.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.*;

public class VoiceVoxManager {
    private static final Logger LOGGER = LogManager.getLogger(VoiceVoxManager.class);
    private static final Gson GSON = new Gson();
    private static final VoiceVoxManager INSTANCE = new VoiceVoxManager();
    private final Map<String, Integer> LOADING_ENGINES = new HashMap<>();
    private final Map<String, Long> LAST_LOAD_ENGINE_TIMES = new HashMap<>();
    private List<VVVoiceType> SPEAKERS;
    private long lastSpeakersLoadTime;

    public static VoiceVoxManager getInstance() {
        return INSTANCE;
    }

    public String getEngineURL() {
        List<String> mostEngines = new ArrayList<>();
        int mct = Integer.MAX_VALUE;
        for (String voiceVoxURL : Main.CONFIG.voiceVoxURLs()) {
            int ct = getLoadingEnginesCount(voiceVoxURL);
            if (ct < mct) {
                mostEngines.clear();
                mostEngines.add(voiceVoxURL);
                mct = ct;
            } else if (ct == mct) {
                mostEngines.add(voiceVoxURL);
            }
        }

        var ky = mostEngines.stream().map(n -> {
            synchronized (LAST_LOAD_ENGINE_TIMES) {
                return FNPair.of(n, LAST_LOAD_ENGINE_TIMES.computeIfAbsent(n, v -> 0L));
            }
        }).sorted(Comparator.comparingLong(FNPair::getRight)).toList();

        return ky.get(0).getKey();
    }

    private int getLoadingEnginesCount(String engineURL) {
        synchronized (LOADING_ENGINES) {
            return LOADING_ENGINES.computeIfAbsent(engineURL, n -> 0);
        }
    }

    private void loadStartEngine(String engineURL) {
        synchronized (LAST_LOAD_ENGINE_TIMES) {
            LAST_LOAD_ENGINE_TIMES.put(engineURL, System.currentTimeMillis());
        }
        synchronized (LOADING_ENGINES) {
            int pl = getLoadingEnginesCount(engineURL);
            LOADING_ENGINES.put(engineURL, pl + 1);
        }
    }

    private void loadEndEngine(String engineURL) {
        synchronized (LOADING_ENGINES) {
            int pl = getLoadingEnginesCount(engineURL);
            if (pl > 0)
                LOADING_ENGINES.put(engineURL, pl - 1);
        }
    }

    public List<VVVoiceType> getSpeakers() {
        loadSpeakers();
        return SPEAKERS;
    }

    private synchronized void loadSpeakers() {
        long tim = 1000 * 60 * 10;
        if (SPEAKERS == null)
            tim = 1000 * 60;

        if (System.currentTimeMillis() - lastSpeakersLoadTime < tim)
            return;

        try {
            JsonArray ja;

            var url = getEngineURL();
            loadStartEngine(url);
            try (Reader reader = new InputStreamReader(FNURLUtil.getStream(new URL(url + "/speakers")))) {
                ja = GSON.fromJson(reader, JsonArray.class);
            } finally {
                loadEndEngine(url);
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

        var url = getEngineURL();
        loadStartEngine(url);
        try {
            var ret = FNURLUtil.getResponseByPOST(new URL(String.format(url + "/audio_query?text=%s&speaker=2", text)), "", "", "");
            return GSON.fromJson(ret.getResponseString(), JsonObject.class);
        } finally {
            loadEndEngine(url);
        }
    }

    public InputStream getVoce(JsonObject query, int speakerId) throws IOException, InterruptedException {
        var url = getEngineURL();
        loadStartEngine(url);
        try {
            var hc = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create(url + "/synthesis?speaker=" + speakerId)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(query))).version(HttpClient.Version.HTTP_1_1).build();
            var res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return res.body();
        } finally {
            loadEndEngine(url);
        }
    }
}
