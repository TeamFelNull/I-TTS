package dev.felnull.itts.core.voice.coeiroink;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Coeiroink系エンジンの管理
 *
 * @author MORIMORI0317
 */
public class CoeiroinkManager {

    /**
     * GSON
     */
    private static final Gson GSON = new Gson();

    /**
     * Coeiroink系の声カテゴリ
     */
    private final CoeiroinkVoiceCategory category = new CoeiroinkVoiceCategory(this);

    /**
     * バランサー
     */
    private final CoeiroinkBalancer balancer;

    /**
     * エンジンのの前
     */
    private final String name;

    /**
     * Coeiroink系エンジンのコンフィグ
     */
    private final Supplier<VoicevoxConfig> configSupplier;

    /**
     * コンストラクタ
     *
     * @param name           名前
     * @param enginUrls      エンジンURL
     * @param configSupplier コンフィグ
     */
    public CoeiroinkManager(String name, Supplier<List<String>> enginUrls, Supplier<VoicevoxConfig> configSupplier) {
        this.name = name;
        this.configSupplier = configSupplier;
        this.balancer = new CoeiroinkBalancer(this, enginUrls);
    }

    protected VoicevoxConfig getConfig() {
        return configSupplier.get();
    }

    /**
     * 初期化
     *
     * @return 初期化の非同期CompletableFuture
     */
    public CompletableFuture<?> init() {
        return balancer.init();
    }

    public CoeiroinkVoiceCategory getCategory() {
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
                .map(r -> (VoiceType) new CoeiroinkVoiceType(r, this))
                .toList();
    }

    protected CoeiroinkBalancer getBalancer() {
        return balancer;
    }

    /**
     * エンジンのURLから話者一覧を取得
     *
     * @param vvurl VOICEVOXのURL
     * @return 話者のリスト
     * @throws IOException          IO例外
     * @throws InterruptedException 割り込み例外
     */
    protected List<CoeiroinkSpeaker> requestSpeakers(CIURL vvurl) throws IOException, InterruptedException {
        HttpClient hc = ITTSRuntime.getInstance().getNetworkManager().getHttpClient();
        HttpRequest req = HttpRequest.newBuilder(vvurl.createURI("speakers"))
                .timeout(Duration.of(3000, ChronoUnit.MILLIS))
                .build();
        HttpResponse<InputStream> rep = hc.send(req, HttpResponse.BodyHandlers.ofInputStream());
        JsonArray ja;

        try (InputStream stream = new BufferedInputStream(rep.body()); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            ja = GSON.fromJson(reader, JsonArray.class);
        }

        ImmutableList.Builder<CoeiroinkSpeaker> speakerBuilder = new ImmutableList.Builder<>();

        for (JsonElement je : ja) {
            speakerBuilder.add(CoeiroinkSpeaker.of(je.getAsJsonObject()));
        }

        return speakerBuilder.build();
    }

    /**
     * 読み上げ音声データのストリームを開く
     *
     * @param text      読み上げるテキスト
     * @param styleId スタイルID
     * @return 音声データのストリーム
     */
    protected InputStream openVoiceStream(String text, int styleId, String speakerUuid) {
        JsonObject qry = createSynthesisParam(text, styleId, speakerUuid);
        try (var urlUse = balancer.getUseURL()) {
            HttpClient hc = ITTSRuntime.getInstance().getNetworkManager().getHttpClient();
            HttpRequest request = HttpRequest.newBuilder(urlUse.getCIURL().createURI("synthesis"))
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(qry)))
                    .build();

            HttpResponse<InputStream> res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());

            Optional<String> content = res.headers().firstValue("content-type");
            int code = res.statusCode();

            if (content.isEmpty()) {
                throw new IOException("Content Type does not exist: " + code);
            }


            if (content.get().startsWith("audio/")) {
                return res.body();
            }

            throw new IOException("Not audio data: " + code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 音声合成用のパラメータを作成する
     * このメソッドは、Coeiroink音声合成エンジンに渡すための設定値を含むJSONオブジェクトを生成する
     * 
     * @param text 合成したいテキスト
     * @param styleId スタイルID（発話スタイルを指定）
     * @param speakerUuid スピーカーのUUID
     * @return 音声合成用のパラメータを含むJSONオブジェクト
     * @see dev.felnull.itts.core.voice.coeiroink.CoeiroinkManager
     */
    private JsonObject createSynthesisParam(String text, int styleId, String speakerUuid) {

        JsonObject param = new JsonObject();

        // TODO : 全部のKeyが必須かを検証する
        // 数値型のパラメータ
        param.addProperty("volumeScale", 1.0);
        param.addProperty("pitchScale", 0);
        param.addProperty("intonationScale", 1.0);
        param.addProperty("prePhonemeLength", 0.1);
        param.addProperty("postPhonemeLength", 0.1);
        param.addProperty("outputSamplingRate", 24000);
        param.addProperty("sampledIntervalValue", 10);
        param.addProperty("startTrimBuffer", 0.0);
        param.addProperty("endTrimBuffer", 0.0);
        param.addProperty("pauseLength", 0.0);
        param.addProperty("pauseStartTrimBuffer", 0.0);
        param.addProperty("pauseEndTrimBuffer", 0.0);
        param.addProperty("speedScale", 1.0);

        // 文字列型のパラメータ
        param.addProperty("speakerUuid", speakerUuid);
        param.addProperty("styleId", styleId);
        param.addProperty("text", text);

        param.addProperty("processingAlgorithm", "default:orig_sr=44100,target_sr=24000");
        param.add("prosodyDetail", null);
        return param;
    }
}
