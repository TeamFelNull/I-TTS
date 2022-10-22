package dev.felnull.ttsvoice.voice.vvengine;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import dev.felnull.fnjl.tuple.FNPair;
import dev.felnull.fnjl.util.FNURLUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class VVEngineManager {
    private static final Logger LOGGER = LogManager.getLogger(VVEngineManager.class);
    private static final Gson GSON = new Gson();
    private final Map<String, Integer> LOADING_ENGINES = new HashMap<>();
    private final Map<String, Long> LAST_LOAD_ENGINE_TIMES = new HashMap<>();
    private List<VVEVoiceType> SPEAKERS;
    private List<String> aliveURLs;
    private long lastSpeakersLoadTime;
    private boolean lastError;

    abstract public List<String> getAllEngineURLs();

    public synchronized List<String> getAliveEngineURLs() {
        if (aliveURLs == null) {
            aliveURLs = new ArrayList<>();
            aliveCheck();
        }

        synchronized (aliveURLs) {
            return ImmutableList.copyOf(aliveURLs);
        }
    }

    abstract protected VVEVoiceType createVoiceType(JsonObject jo, String name, boolean neta);

    abstract protected String getName();

    public String getEngineURL() {
        List<String> mostEngines = new ArrayList<>();
        int mct = Integer.MAX_VALUE;
        for (String vvEngineURL : getAliveEngineURLs()) {
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

        if (ky.isEmpty())
            throw new RuntimeException(String.format("no %s available", getName()));

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

            var hc = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create(url + "/speakers")).version(HttpClient.Version.HTTP_1_1).timeout(Duration.of(500, ChronoUnit.MILLIS)).build();
            var rep = hc.send(req, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream stream = new BufferedInputStream(rep.body()); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                ja = GSON.fromJson(reader, JsonArray.class);
            } finally {
                loadEndEngine(url);
            }

            ImmutableList.Builder<VVEVoiceType> speakers = new ImmutableList.Builder<>();

            for (JsonElement element : ja) {
                var jo = (JsonObject) element;
                var name = jo.get("name").getAsString();
                var styles = jo.getAsJsonArray("styles");
                boolean neta = false;
                for (JsonElement style : styles) {
                    var sjo = (JsonObject) style;
                    speakers.add(createVoiceType(sjo, name, neta));
                    neta = true;
                }
            }

            if (SPEAKERS == null)
                LOGGER.info("Successful get of " + getName() + " speakers");

            SPEAKERS = speakers.build();
            lastError = false;
            lastSpeakersLoadTime = System.currentTimeMillis();
        } catch (Exception e) {
            //    if (!lastError)
            //       LOGGER.error("Failed to get " + getName() + " speakers", e);
            if (!lastError) {
                String errorName = e.getLocalizedMessage();
                if (e instanceof JsonSyntaxException)
                    errorName = "Json error";

                if (e instanceof RuntimeException) {
                    LOGGER.error("Failed to connect " + getName() + " server");
                } else {
                    LOGGER.error("Failed to connect " + getName() + " server: " + errorName);
                }
            }

            lastError = true;
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
        var ret = loadVoice(query, speakerId, "synthesis");
        //   var ret = loadVoice(query, speakerId, "cancellable_synthesis");

        if (ret.getRight().firstValue("content-type").map(v -> v.startsWith("audio/")).orElse(false))
            return ret.getLeft();
        return null;
    }

    private Pair<InputStream, HttpHeaders> loadVoice(JsonObject query, int speakerId, String synthesisStr) throws IOException, InterruptedException {
        var url = getEngineURL();
        loadStartEngine(url);
        try {
            var hc = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(URI.create(url + "/" + synthesisStr + "?speaker=" + speakerId)).timeout(Duration.of(10, ChronoUnit.SECONDS)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(query))).version(HttpClient.Version.HTTP_1_1).build();
            var res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return Pair.of(res.body(), res.headers());
        } finally {
            loadEndEngine(url);
        }
    }

    protected boolean aliveCheck() {
        List<String> alive = new ArrayList<>();
        for (String engineURL : getAllEngineURLs()) {
            try {
                var hc = HttpClient.newHttpClient();
                var request = HttpRequest.newBuilder(URI.create(engineURL + "/openapi.json")).timeout(Duration.of(10, ChronoUnit.SECONDS)).GET().version(HttpClient.Version.HTTP_1_1).build();
                var res = hc.send(request, HttpResponse.BodyHandlers.ofString());
                GSON.fromJson(res.body(), JsonObject.class);
                alive.add(engineURL);
            } catch (Exception ignored) {
            }
        }

        if (aliveURLs != null) {
            synchronized (aliveURLs) {
                aliveURLs.clear();
                aliveURLs.addAll(alive);
                synchronized (LOADING_ENGINES) {
                    for (String s : LOADING_ENGINES.keySet()) {
                        if (!aliveURLs.contains(s))
                            LOADING_ENGINES.remove(s);
                    }
                }

                synchronized (LAST_LOAD_ENGINE_TIMES) {
                    for (String s : LAST_LOAD_ENGINE_TIMES.keySet()) {
                        if (!aliveURLs.contains(s))
                            LAST_LOAD_ENGINE_TIMES.remove(s);
                    }
                }
            }
        }

        return !alive.isEmpty();
    }

    abstract public boolean isAlive();
}
