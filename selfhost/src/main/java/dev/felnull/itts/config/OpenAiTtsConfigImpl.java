package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.OpenAiTtsConfig;
import dev.felnull.itts.utils.Json5Utils;

import java.net.URI;
import java.util.List;

/**
 * OpenAI互換TTSコンフィグの実装
 *
 * @param enable 有効かどうか
 * @param baseUrl APIベースURL
 * @param apiKey APIキー
 * @param model モデル名
 * @param voices 話者一覧
 */
public record OpenAiTtsConfigImpl(boolean enable, String baseUrl, String apiKey, String model, List<String> voices) implements OpenAiTtsConfig {
    /**
     * コンストラクタ
     *
     * @param enable 有効かどうか
     * @param baseUrl APIベースURL
     * @param apiKey APIキー
     * @param model モデル名
     * @param voices 話者一覧
     */
    public OpenAiTtsConfigImpl {
        voices = List.copyOf(voices);
        if (enable) {
            URI uri = URI.create(baseUrl);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("openai_tts.base_url must be an HTTP(S) base URL without credentials, query or fragment");
            }
            if (model.isBlank() || voices.isEmpty() || voices.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("openai_tts.model and voices must not be empty");
            }
        }
    }

    /**
     * 初期設定を作成
     */
    public OpenAiTtsConfigImpl() {
        this(false, "https://api.openai.com/v1", "", "tts-1", List.of("alloy"));
    }

    /**
     * JSONから読み込む
     *
     * @param json JSONオブジェクト
     * @return コンフィグ
     */
    public static OpenAiTtsConfigImpl fromJson(JsonObject json) {
        OpenAiTtsConfigImpl defaults = new OpenAiTtsConfigImpl();
        return new OpenAiTtsConfigImpl(
                json.getBoolean("enable", defaults.enable()),
                Json5Utils.getStringOrElse(json, "base_url", defaults.baseUrl()),
                Json5Utils.getStringOrElse(json, "api_key", defaults.apiKey()),
                Json5Utils.getStringOrElse(json, "model", defaults.model()),
                json.containsKey("voices") ? Json5Utils.getStringListOfJsonArray(json, "voices") : defaults.voices());
    }

    /**
     * JSONへ変換
     *
     * @return JSONオブジェクト
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("enable", JsonPrimitive.of(enable), "OpenAI互換TTSを有効にするかどうか");
        json.put("base_url", JsonPrimitive.of(baseUrl), "APIベースURL (/audio/speechを自動追加)");
        json.put("api_key", JsonPrimitive.of(apiKey), "APIキー (認証不要の場合は空文字列)");
        json.put("model", JsonPrimitive.of(model), "音声合成モデル名");
        json.put("voices", Json5Utils.toJsonArray(voices), "利用する話者名の一覧");
        return json;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public List<String> getVoices() {
        return voices;
    }
}
