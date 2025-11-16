package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;

/**
 * コンフィグローダー
 *
 * @param <T> コンフィグクラス
 */
public interface ConfigLoader<T> {

    /**
     * JSONから読み込む
     *
     * @param json5 Json
     * @return 読み込んだコンフィグ
     */
    T load(JsonObject json5);

    /**
     * ひとつ前のバージョンのコンフィグから移行して読み込む
     *
     * @param oldConfig 古いコンフィグ
     * @return 読み込んだコンフィグ
     */
    T migrate(Object oldConfig);
}
