package dev.felnull.itts.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Prometheusメトリクスのレジストリ管理
 */
public final class MetricsRegistry {

    /**
     * Prometheus形式のレジストリ
     */
    private final PrometheusMeterRegistry registry;

    /**
     * 起動時刻のミリ秒
     */
    private final long bootAt;

    /**
     * 文字数Counterのキャッシュ
     */
    private final ConcurrentMap<String, Counter> charCounters = new ConcurrentHashMap<>();

    /**
     * メッセージ数Counterのキャッシュ
     */
    private final ConcurrentMap<String, Counter> messageCounters = new ConcurrentHashMap<>();

    /**
     * コンストラクタ
     */
    public MetricsRegistry() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.bootAt = System.currentTimeMillis();

        Gauge.builder("itts_up", () -> 1.0d)
                .description("Bot liveness indicator")
                .register(registry);

        Gauge.builder("itts_uptime_seconds", () -> (System.currentTimeMillis() - bootAt) / 1000.0d)
                .description("Bot uptime in seconds")
                .register(registry);

        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
    }

    /**
     * Prometheusレジストリを取得
     *
     * @return レジストリ
     */
    @NotNull
    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    /**
     * 文字数Counterをラベル付きで取得
     *
     * @param botId    BOTのID
     * @param serverId サーバーID nullの場合はBOT全体
     * @return Counter
     */
    @NotNull
    public Counter getOrCreateCharCounter(long botId, Long serverId) {
        String key = botId + "|" + (serverId == null ? "global" : serverId.toString());
        return charCounters.computeIfAbsent(key, k -> Counter.builder("itts_spoken_chars_total")
                .description("Total spoken characters delivered to TTS API")
                .tag("bot_id", String.valueOf(botId))
                .tag("server_id", serverId == null ? "global" : String.valueOf(serverId))
                .register(registry));
    }

    /**
     * メッセージ数Counterをラベル付きで取得
     *
     * @param botId    BOTのID
     * @param serverId サーバーID nullの場合はBOT全体
     * @return Counter
     */
    @NotNull
    public Counter getOrCreateMessageCounter(long botId, Long serverId) {
        String key = botId + "|" + (serverId == null ? "global" : serverId.toString());
        return messageCounters.computeIfAbsent(key, k -> Counter.builder("itts_spoken_messages_total")
                .description("Total spoken messages delivered to TTS API")
                .tag("bot_id", String.valueOf(botId))
                .tag("server_id", serverId == null ? "global" : String.valueOf(serverId))
                .register(registry));
    }
}
