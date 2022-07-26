package dev.felnull.ttsvoice.voice.vvengine;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.fnjl.util.FNURLUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class VVEngineManager {
    private static final Logger LOGGER = LogManager.getLogger(VVEngineManager.class);
    private static final Gson GSON = new Gson();
    private final Map<String, Integer> LOADING_ENGINES = new HashMap<>();
    private final Map<String, Long> LAST_LOAD_ENGINE_TIMES = new HashMap<>();
    private List<VVEVoiceType> SPEAKERS;
    private long lastSpeakersLoadTime;

    abstract public List<String> getEngineURLs();

    abstract protected VVEVoiceType createVoiceType(JsonObject jo, String name);

    abstract protected String getName();

    public String getEngineURL() {
        List<String> mostEngines = new ArrayList<>();
        int mct = Integer.MAX_VALUE;
        for (String vvEngineURL : getEngineURLs()) {
            int ct = getLoadingEnginesCount(vvEngineURL);
            if (ct < mct) {
                mostEngines.clear();
                mostEngines.add(vvEngineURL);
                mct = ct;
            } else if (ct == mct) {
                mostEngines.add(vvEngineURL);
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

    public List<VVEVoiceType> getSpeakers() {
        loadSpeakers();
        if (SPEAKERS == null) return ImmutableList.of();
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

            ImmutableList.Builder<VVEVoiceType> speakers = new ImmutableList.Builder<>();

            for (JsonElement element : ja) {
                var jo = (JsonObject) element;
                var name = jo.get("name").getAsString();
                var styles = jo.getAsJsonArray("styles");
                for (JsonElement style : styles) {
                    var sjo = (JsonObject) style;
                    speakers.add(createVoiceType(sjo, name));
                }
            }

            if (SPEAKERS == null)
                LOGGER.info("Successful get of " + getName() + " speakers");

            SPEAKERS = speakers.build();

            lastSpeakersLoadTime = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.error("Failed to get " + getName() + " speakers", e);
            lastSpeakersLoadTime = System.currentTimeMillis() - 1000 * 60 * 7;
        }
    }

    public JsonObject getQuery(String text, int speakerId) throws URISyntaxException, IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);
        text = new URI(text).toASCIIString();

        var url = getEngineURL();
        loadStartEngine(url);
        try {
            var ret = FNURLUtil.getResponseByPOST(new URL(String.format(url + "/audio_query?text=%s&speaker=" + speakerId, text)), "", "", "");
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
