package dev.felnull.itts.core.voice.openai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.config.voicetype.OpenAiTtsConfig;
import dev.felnull.itts.core.voice.VoiceHttpUtils;
import dev.felnull.itts.core.voice.VoiceType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * OpenAI互換TTSの管理
 */
public class OpenAiTtsManager {
    /**
     * コンフィグの取得元
     */
    private final Supplier<OpenAiTtsConfig> config;

    /**
     * 共有HTTPクライアントの取得元
     */
    private final Supplier<HttpClient> httpClient;

    /**
     * 声カテゴリ
     */
    private final OpenAiTtsVoiceCategory category = new OpenAiTtsVoiceCategory(this);

    /**
     * コンストラクタ
     *
     * @param config     コンフィグの取得元
     * @param httpClient 共有HTTPクライアントの取得元
     */
    public OpenAiTtsManager(Supplier<OpenAiTtsConfig> config, Supplier<HttpClient> httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    /**
     * 利用可能かどうかを取得
     *
     * @return 有効な接続設定と話者が存在する場合はtrue
     */
    public boolean isAvailable() {
        OpenAiTtsConfig settings = config.get();
        return settings.isEnable() && !settings.getBaseUrl().isBlank() && !settings.getModel().isBlank()
                && settings.getVoices().stream().anyMatch(voice -> !voice.isBlank());
    }

    /**
     * 声タイプ一覧を取得
     *
     * @return 設定された声タイプの一覧
     */
    public List<VoiceType> getVoiceTypes() {
        if (!isAvailable()) {
            return List.of();
        }
        return config.get().getVoices().stream().filter(voice -> !voice.isBlank()).distinct()
                .map(voice -> (VoiceType) new OpenAiTtsVoiceType(this, voice)).toList();
    }

    /**
     * 声カテゴリを取得
     *
     * @return 声カテゴリ
     */
    public OpenAiTtsVoiceCategory getCategory() {
        return category;
    }

    /**
     * キャッシュを接続先とモデルごとに区別する文字列を取得
     *
     * @return キャッシュ識別子
     */
    public String getCacheIdentity() {
        OpenAiTtsConfig settings = config.get();
        JsonArray identity = new JsonArray();
        identity.add(settings.getBaseUrl());
        identity.add(settings.getModel());
        identity.add("wav");
        return identity.toString();
    }

    /**
     * 音声ストリームを開く
     *
     * @param voice 話者名
     * @param text  読み上げるテキスト
     * @return 呼び出し元で閉じる音声ストリーム
     * @throws IOException          HTTP通信またはAPI応答の異常
     * @throws InterruptedException 割り込み例外
     */
    public InputStream openVoiceStream(String voice, String text) throws IOException, InterruptedException {
        OpenAiTtsConfig settings = config.get();
        if (!isAvailable() || !settings.getVoices().contains(voice)) {
            throw new IOException("OpenAI compatible TTS voice is not available");
        }
        JsonObject body = new JsonObject();
        body.addProperty("model", settings.getModel());
        body.addProperty("input", text);
        body.addProperty("voice", voice);
        body.addProperty("response_format", "wav");

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(settings.getBaseUrl().replaceAll("/+$", "") + "/audio/speech"))
                .header("Content-Type", "application/json")
                .timeout(VoiceHttpUtils.SYNTHESIS_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (!settings.getApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + settings.getApiKey());
        }

        HttpResponse<InputStream> response;
        try {
            response = httpClient.get().send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (HttpTimeoutException e) {
            throw VoiceHttpUtils.timeoutException("OpenAI compatible TTS", "audio/speech", e);
        }
        String contentType = response.headers().firstValue("content-type").orElse("").toLowerCase(Locale.ROOT);
        if (response.statusCode() >= 200 && response.statusCode() < 300
                && (contentType.startsWith("audio/") || contentType.split(";", 2)[0].trim().equals("application/octet-stream"))) {
            return response.body();
        }
        response.body().close();
        throw new IOException("OpenAI compatible TTS returned invalid audio (HTTP " + response.statusCode() + ")");
    }
}
