package dev.felnull.itts.core.metrics;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * PrometheusメトリクスをHTTPで公開するエクスポーザ
 */
public final class PrometheusHttpExposer {

    /**
     * メトリクスレジストリ
     */
    private final MetricsRegistry metricsRegistry;

    /**
     * 内部HTTPサーバー
     */
    private HttpServer httpServer;

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
        server.setExecutor(null);
        server.start();
        this.httpServer = server;
    }

    /**
     * 停止する
     */
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
}
