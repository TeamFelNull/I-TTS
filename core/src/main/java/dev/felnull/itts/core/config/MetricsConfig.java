package dev.felnull.itts.core.config;

import org.jetbrains.annotations.NotNull;

/**
 * Prometheusメトリクス公開のコンフィグ
 */
public interface MetricsConfig {

    /**
     * デフォルトの有効状態
     */
    boolean DEFAULT_ENABLED = false;

    /**
     * デフォルトのバインドアドレス
     */
    String DEFAULT_BIND_ADDRESS = "127.0.0.1";

    /**
     * デフォルトのポート番号
     */
    int DEFAULT_PORT = 9095;

    /**
     * デフォルトのコンフィグインスタンス
     */
    MetricsConfig DEFAULT = new MetricsConfig() {
        @Override
        public boolean isEnabled() {
            return DEFAULT_ENABLED;
        }

        @Override
        public @NotNull String getBindAddress() {
            return DEFAULT_BIND_ADDRESS;
        }

        @Override
        public int getPort() {
            return DEFAULT_PORT;
        }
    };

    /**
     * 有効かどうかを取得
     *
     * @return 有効かどうか
     */
    boolean isEnabled();

    /**
     * バインドアドレスを取得
     *
     * @return バインドアドレス
     */
    @NotNull
    String getBindAddress();

    /**
     * ポート番号を取得
     *
     * @return ポート番号
     */
    int getPort();
}
