package dev.felnull.itts.core.config.voicetype;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * VOICEVOX系共通コンフィグ
 *
 * @author MORIMORI0317
 */
public interface VoicevoxConfig extends VoiceTypeConfig {
    /**
     * デフォルトのエンジンURL
     */
    List<String> DEFAULT_API_URLS = ImmutableList.of("");

    /**
     * デフォルトの確認間隔
     */
    long DEFAULT_CHECK_TIME = 15000;

    /**
     * エンジンURLのリスト
     *
     * @return URLのリスト
     */
    @NotNull
    @Unmodifiable
    List<String> getApiUrls();

    /**
     * 確認間隔
     *
     * @return 確認間隔(ms)
     */
    long getCheckTime();
}
