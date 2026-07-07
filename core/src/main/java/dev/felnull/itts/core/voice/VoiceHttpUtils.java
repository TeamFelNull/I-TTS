package dev.felnull.itts.core.voice;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 音声合成APIのHTTP処理で使う共通値と例外生成
 *
 * @author Codex
 */
public final class VoiceHttpUtils {

    /**
     * 話者一覧取得のタイムアウト
     */
    public static final Duration SPEAKER_LIST_TIMEOUT = Duration.of(3000, ChronoUnit.MILLIS);

    /**
     * 音声合成リクエストのタイムアウト
     */
    public static final Duration SYNTHESIS_TIMEOUT = Duration.of(30, ChronoUnit.SECONDS);

    private VoiceHttpUtils() {
    }

    /**
     * タイムアウト例外を音声API名つきのIO例外へ変換する
     *
     * @param engineName エンジン名
     * @param apiName    API名
     * @param exception  タイムアウト例外
     * @return IO例外
     */
    public static IOException timeoutException(String engineName, String apiName, HttpTimeoutException exception) {
        return new IOException(engineName + " " + apiName + " API timed out after " + SYNTHESIS_TIMEOUT.toSeconds() + " seconds", exception);
    }
}
