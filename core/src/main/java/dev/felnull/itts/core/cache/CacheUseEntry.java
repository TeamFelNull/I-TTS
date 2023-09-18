package dev.felnull.itts.core.cache;

import java.io.File;

/**
 * キャッシュエントリ
 *
 * @param file    保存先ファイル
 * @param useLock 使用中か確認用ロック
 * @author MORIMORI0317
 */
public record CacheUseEntry(File file, UseLock useLock) {
}
