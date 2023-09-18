package dev.felnull.itts.core.voice;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.felnull.itts.core.audio.loader.CachedVoiceTrackLoader;
import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * キャッシュを行う声
 *
 * @author MORIMORI0317
 */
public abstract class CachedVoice extends BaseVoice {

    /**
     * コンストラクタ
     *
     * @param voiceType 声タイプ
     */
    protected CachedVoice(VoiceType voiceType) {
        super(voiceType);
    }

    /**
     * 声データのストリームを開く
     *
     * @param text 読み上げるテキスト
     * @return 声データのストリーム
     * @throws IOException          IO例外
     * @throws InterruptedException 割り込み例外
     */
    protected abstract InputStream openVoiceStream(String text) throws IOException, InterruptedException;

    /**
     * 声のハッシュ文字列を求める
     *
     * @return ハッシュの文字列
     */
    protected abstract String createHashCodeChars();

    @Override
    public VoiceTrackLoader createVoiceTrackLoader(String text) {
        HashCode hash = Hashing.murmur3_128().newHasher()
                .putString(voiceType.getId(), StandardCharsets.UTF_8)
                .putString(text, StandardCharsets.UTF_8)
                .putString(createHashCodeChars(), StandardCharsets.UTF_8)
                .hash();

        return new CachedVoiceTrackLoader(hash, () -> {
            if (!isAvailable()) {
                throw new RuntimeException("Voice is not available");
            }

            return openVoiceStream(text);
        });
    }
}
