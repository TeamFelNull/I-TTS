package dev.felnull.itts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * 標準入力から停止コマンドを受け付ける
 * パネルなどが終了時に標準入力へ送る{@code stop}を想定する
 */
final class StopCommandListener {

    /**
     * 停止コマンド
     */
    private static final String STOP_COMMAND = "stop";

    private StopCommandListener() {
    }

    /**
     * 標準入力の監視を別スレッドで開始する
     *
     * @param input  入力
     * @param onStop 停止コマンドを受け取った時の処理
     */
    static void start(InputStream input, Runnable onStop) {
        Thread thread = new Thread(() -> listen(input, onStop), "stop-command-listener");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 停止コマンドを受け取るまで入力を読み続ける
     * 入力が閉じられた場合は何もせず終了する
     *
     * @param input  入力
     * @param onStop 停止コマンドを受け取った時の処理
     */
    static void listen(InputStream input, Runnable onStop) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (STOP_COMMAND.equalsIgnoreCase(line.strip())) {
                    onStop.run();
                    return;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
