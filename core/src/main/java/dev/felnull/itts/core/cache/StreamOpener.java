package dev.felnull.itts.core.cache;

import java.io.IOException;
import java.io.InputStream;

/**
 * ストリームを開くためのインターフェイス
 *
 * @author MORIMORI0317
 */
public interface StreamOpener {
    /**
     * ストリームを開く
     *
     * @return ストリーム
     * @throws IOException          IO例外
     * @throws InterruptedException 割り込み例外
     */
    InputStream openStream() throws IOException, InterruptedException;
}
