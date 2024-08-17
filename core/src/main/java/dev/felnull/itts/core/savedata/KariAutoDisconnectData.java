package dev.felnull.itts.core.savedata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自動切断機能に使うデータの仮置き
 *
 * @author MORIMORI0317
 */
public final class KariAutoDisconnectData {

    /**
     * モード保存用マップ
     */
    private static final Map<Long, Mode> MODES = new ConcurrentHashMap<>();

    /**
     * 再接続予定のチャンネル
     */
    private static final Map<Long, TTSChannelPair> RECONNECT_CHANNELS = new ConcurrentHashMap<>();

    private KariAutoDisconnectData() {
    }

    /**
     * 自動切断モードを取得
     *
     * @param guildId サーバーID
     * @return モード
     */
    public static Mode getMode(long guildId) {
        return MODES.getOrDefault(guildId, Mode.OFF);
    }

    /**
     * 自動切断モードを変更
     *
     * @param guildId サーバーID
     * @param mode    モード
     */
    public static void setMode(long guildId, Mode mode) {
        MODES.put(guildId, mode);
    }

    /**
     * 再接続を行う予定のチャンネルのペア
     *
     * @param guildId サーバーID
     * @return チャンネル
     */
    public static TTSChannelPair getReconnectChannel(long guildId) {
        return RECONNECT_CHANNELS.computeIfAbsent(guildId, key -> new TTSChannelPair(-1, -1));
    }

    /**
     * 再接続を行う予定のチャンネルを変更
     *
     * @param guildId    サーバーID
     * @param ttsChannel チャンネル
     */
    public static void setReconnectChannel(long guildId, TTSChannelPair ttsChannel) {
        RECONNECT_CHANNELS.put(guildId, ttsChannel);
    }

    /**
     * 自動切断のモード
     *
     * @author MORIMORI0317
     */
    public enum Mode {

        /**
         * 無効
         */
        OFF(false, false),

        /**
         * 有効
         */
        ON(true, false),

        /**
         * 有効 (再起動あり)
         */
        ON_RECONNECT(true, true);

        /**
         * 自動切断が有効かどうか
         */
        private final boolean enable;

        /**
         * 自動切断後、再参加時に再接続を行うかどうか
         */
        private final boolean reconnect;

        Mode(boolean enable, boolean reconnect) {
            this.enable = enable;
            this.reconnect = reconnect;
        }

        public boolean isEnable() {
            return enable;
        }

        public boolean isReconnect() {
            return reconnect;
        }
    }

    /**
     * 読み上げと実際に発言するチャンネルのペア
     *
     * @param speakAudioChannel     発言するオーディオチャンネル
     * @param readAroundTextChannel 読み上げるテキストチャンネル
     * @author MORIMORI0317
     */
    public record TTSChannelPair(long speakAudioChannel, long readAroundTextChannel) {
    }
}
