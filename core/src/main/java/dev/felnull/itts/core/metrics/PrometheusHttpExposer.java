package dev.felnull.itts.core.metrics;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * PrometheusメトリクスをHTTPで公開するエクスポーザ
 */
public final class PrometheusHttpExposer {

    /**
     * リクエスト処理スレッドプールのスレッド数
     */
    private static final int EXECUTOR_THREAD_COUNT = 2;

    /**
     * Executor停止時の待機秒数
     */
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SEC = 5;

    /**
     * メトリクスレジストリ
     */
    private final MetricsRegistry metricsRegistry;

    /**
     * 内部HTTPサーバー
     */
    private HttpServer httpServer;

    /**
     * リクエスト処理用スレッドプール
     */
    private ExecutorService executor;

    /**
     * コンストラクタ
     *
     * @param metricsRegistry メトリクスレジストリ
     */
    public PrometheusHttpExposer(@NotNull MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    /**
     * 起動する
     *
     * @param host バインドアドレス
     * @param port ポート番号
     * @throws IOException 起動失敗時
     */
    public void start(@NotNull String host, int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/metrics", exchange -> {
            String body = metricsRegistry.getRegistry().scrape();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(EXECUTOR_THREAD_COUNT,
                new BasicThreadFactory.Builder()
                        .namingPattern("prometheus-exposer-%d")
                        .daemon(true)
                        .build());
        server.setExecutor(pool);
        server.start();
        this.httpServer = server;
        this.executor = pool;
    }

    /**
     * 停止する
     */
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }
}
