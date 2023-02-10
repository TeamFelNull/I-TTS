package dev.felnull.itts.core.voice.voicevox;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.config.voicetype.VoiceVoxEngineBaseVoiceTypeConfig;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.*;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class VoicevoxManager {
    private static final Gson GSON = new Gson();
    private final VoicevoxVoiceCategory category = new VoicevoxVoiceCategory(this);
    private final VoicevoxBalancer balancer;
    private final String name;
    private final Supplier<VoiceVoxEngineBaseVoiceTypeConfig> configSupplier;

    public VoicevoxManager(String name, Supplier<List<String>> enginUrls, Supplier<VoiceVoxEngineBaseVoiceTypeConfig> configSupplier) {
        this.name = name;
        this.configSupplier = configSupplier;
        this.balancer = new VoicevoxBalancer(this, enginUrls);
    }

    protected VoiceVoxEngineBaseVoiceTypeConfig getConfig() {
        return configSupplier.get();
    }

    public CompletableFuture<?> init() {
        return balancer.init();
    }

    public VoicevoxVoiceCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return getConfig().isEnable() && balancer.isAvailable();
    }

    public List<VoiceType> getAvailableVoiceTypes() {
        return balancer.getAvailableSpeakers().stream()
                .map(r -> (VoiceType) new VoicevoxVoiceType(r, this))
                .toList();
    }

    protected List<VoicevoxSpeaker> requestSpeakers(VVURL vvurl) throws IOException, InterruptedException {
        var hc = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(vvurl.createURI("speakers")).version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.of(3000, ChronoUnit.MILLIS))
                .build();
        var rep = hc.send(req, HttpResponse.BodyHandlers.ofInputStream());
        JsonArray ja;

        try (InputStream stream = new BufferedInputStream(rep.body()); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            ja = GSON.fromJson(reader, JsonArray.class);
        }

        ImmutableList.Builder<VoicevoxSpeaker> speakerBuilder = new ImmutableList.Builder<>();

        for (JsonElement je : ja) {
            speakerBuilder.add(VoicevoxSpeaker.of(je.getAsJsonObject()));
        }

        return speakerBuilder.build();
    }

    private JsonObject getQuery(String text, int speakerId) {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);

        try (var urlUse = balancer.getUseURL()) {
            var hc = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(urlUse.getVVURL().createURI(String.format("audio_query?text=%s&speaker=%d", text, speakerId)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build();
            var rep = hc.send(req, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream stream = new BufferedInputStream(rep.body()); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, JsonObject.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected InputStream openVoiceStream(String text, int speakerId) throws IOException, InterruptedException {
        var qry = getQuery(text, speakerId);
        try (var urlUse = balancer.getUseURL()) {
            var hc = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder(urlUse.getVVURL().createURI(String.format("synthesis?speaker=%d", speakerId)))
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(qry)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            var res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());

            var content = res.headers().firstValue("content-type");
            int code = res.statusCode();

            if (content.isEmpty())
                throw new IOException("Content Type does not exist: " + code);

            if (content.get().startsWith("audio/"))
                return res.body();

            throw new IOException("Not audio data: " + code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
