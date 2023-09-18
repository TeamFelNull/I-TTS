package dev.felnull.itts.core.cache;

/**
 * キャッシュのロック解除用
 *
 * @author MORIMORI0317
 */
public interface UseLock {
    /**
     * ロックを解除
     */
    void unlock();
}
