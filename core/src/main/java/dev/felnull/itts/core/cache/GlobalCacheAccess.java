package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import org.jetbrains.annotations.NotNull;

/**
 * グローバルキャッシュへのアクセス用インターフェイス
 *
 * @author MORIMORI0317
 */
public interface GlobalCacheAccess extends AutoCloseable {

    /**
     * キャッシュを取得
     *
     * @param hashCode キーとしてのハッシュコード
     * @return キャッシュデータ
     */
    byte[] get(@NotNull HashCode hashCode);

    /**
     * キャッシュを保存
     *
     * @param hashCode キーとしてのハッシュコード
     * @param data     キャッシュデータ
     */
    void set(@NotNull HashCode hashCode, byte[] data);

    /**
     * 指定されたキャッシュをロックする
     *
     * @param hashCode キーとしてのハッシュコード
     */
    void lock(@NotNull HashCode hashCode);

    /**
     * 指定されたキャッシュのロックを解除する
     *
     * @param hashCode キーとしてのハッシュコード
     */
    void unlock(@NotNull HashCode hashCode);
}
