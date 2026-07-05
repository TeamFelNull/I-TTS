package dev.felnull.itts.core.voice.voicetext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.felnull.fnjl.util.FNStringUtil;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.voice.VoiceHttpUtils;
import dev.felnull.itts.core.voice.VoiceType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * VoiceTextの管理
 *
 * @author MORIMORI0317
 */
public class VoiceTextManager implements ITTSRuntimeUse {

    /**
     * APIのURL
     */
    private static final String API_URL = "https://api.voicetext.jp/v1/tts";

    /**
     * GSON
     */
    private static final Gson GSON = new Gson();

    /**
     * VoiceTextの声カテゴリ
     */
    private final VoiceTextVoiceCategory category = new VoiceTextVoiceCategory();

    /**
     * 全てのボイスタイプ
     */
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

    /**
     * VoiceTextが利用可能かどうかを取得
     *
     * @return 利用可能な場合はtrue
     */
    public boolean isAvailable() {
        String apiKey = getApiKey();
        return getConfigManager().getConfig().getVoiceTextConfig().isEnable() && apiKey != null && !apiKey.isBlank();
    }

    /**
     * 声データのストリームを開く
     *
     * @param speaker 話者
     * @param text    読み上げるテキスト
     * @return 声データのストリーム
     * @throws IOException          IO例外
     * @throws InterruptedException 割り込み例外
     */
    public InputStream openVoiceStream(@NotNull VoiceTextSpeaker speaker, @NotNull String text) throws IOException, InterruptedException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpClient hc = getNetworkManager().getHttpClient();
        String basic = "Basic " + FNStringUtil.encodeBase64(getApiKey() + ":");
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .header("Authorization", basic)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .timeout(VoiceHttpUtils.SYNTHESIS_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(String.format("text=%s&speaker=%s", text, speaker.getId())))
                .build();
        HttpResponse<InputStream> res;

        try {
            res = hc.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (HttpTimeoutException e) {
            throw VoiceHttpUtils.timeoutException("VoiceText", "tts", e);
        }

        Optional<String> content = res.headers().firstValue("content-type");
        int code = res.statusCode();

        if (content.isEmpty()) {
            throw new IOException("Content Type does not exist: " + code);
        }

        if (content.get().startsWith("audio/")) {
            return res.body();
        }

        if (content.get().startsWith("application/json")) {
            try (InputStream stream = new BufferedInputStream(res.body()); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject jo = GSON.fromJson(reader, JsonObject.class);
                throw new IOException("VoiceText error (" + getErrorMessage(jo) + "): " + code);
            } catch (JsonSyntaxException ignored) {
                // Json解析エラーの場合は無視
            }
        }

        throw new IOException("Not audio data: " + code);
    }

    private String getErrorMessage(JsonObject response) {
        if (response == null) {
            return "invalid JSON response";
        }

        JsonElement errorElement = response.get("error");
        if (errorElement != null && errorElement.isJsonObject()) {
            JsonObject error = errorElement.getAsJsonObject();
            JsonElement messageElement = error.get("message");
            if (messageElement != null && messageElement.isJsonPrimitive()) {
                return messageElement.getAsString();
            }
        }

        JsonElement messageElement = response.get("message");
        if (messageElement != null && messageElement.isJsonPrimitive()) {
            return messageElement.getAsString();
        }

        return "unknown error";
    }
}
