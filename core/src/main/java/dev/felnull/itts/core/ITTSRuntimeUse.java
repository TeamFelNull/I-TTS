package dev.felnull.itts.core;

import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheManager;
import dev.felnull.itts.core.config.ConfigManager;
import dev.felnull.itts.core.dict.DictionaryManager;
import dev.felnull.itts.core.discord.Bot;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.tts.TTSManager;
import dev.felnull.itts.core.voice.VoiceManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;

/**
 * ランタイムのメソッドを簡単に取得するためのインターフェイス
 *
 * @author MORIMORI0317
 */
public interface ITTSRuntimeUse {

    default ITTSRuntime getITTSRuntime() {
        return ITTSRuntime.getInstance();
    }

    default Logger getITTSLogger() {
        return getITTSRuntime().getLogger();
    }

    default Executor getAsyncExecutor() {
        return getITTSRuntime().getAsyncWorkerExecutor();
    }

    default Executor getHeavyExecutor() {
        return getITTSRuntime().getHeavyWorkerExecutor();
    }

    /**
     * HTTP接続用エクスキューターを取得
     *
     * @return エクスキューター
     */
    default Executor getHttpExecutor() {
        return getITTSRuntime().getHttpWorkerExecutor();
    }

    default ImmortalityTimer getImmortalityTimer() {
        return getITTSRuntime().getImmortalityTimer();
    }

    default ConfigManager getConfigManager() {
        return getITTSRuntime().getConfigManager();
    }

    default CacheManager getCacheManager() {
        return getITTSRuntime().getCacheManager();
    }

    default VoiceAudioManager getVoiceAudioManager() {
        return getITTSRuntime().getVoiceAudioManager();
    }

    default DictionaryManager getDictionaryManager() {
        return getITTSRuntime().getDictionaryManager();
    }

    default SaveDataManager getSaveDataManager() {
        return getITTSRuntime().getSaveDataManager();
    }

    default VoiceManager getVoiceManager() {
        return getITTSRuntime().getVoiceManager();
    }

    default TTSManager getTTSManager() {
        return getITTSRuntime().getTTSManager();
    }

    default Bot getBot() {
        return getITTSRuntime().getBot();
    }

    /**
     * ネットワーク接続用マネージャーを取得
     *
     * @return ネットワークマネージャー
     */
    default ITTSNetworkManager getNetworkManager() {
        return getITTSRuntime().getNetworkManager();
    }
}
