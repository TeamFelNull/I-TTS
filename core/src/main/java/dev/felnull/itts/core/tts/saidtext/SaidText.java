package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.voice.Voice;

import java.util.concurrent.CompletableFuture;


/**
 * 読み上げテキスト
 *
 * @author MORIMORI0317
 */
public interface SaidText {


    /**
     * 非同期でテキストを取得
     *
     * @return 読み上げる文字列のCompletableFuture
     */
    CompletableFuture<String> getText();

    /**
     * 非同期で読み上げる音声タイプを取得
     *
     * @return 読み上げる音声タイプのCompletableFuture
     */
    CompletableFuture<Voice> getVoice();
}
