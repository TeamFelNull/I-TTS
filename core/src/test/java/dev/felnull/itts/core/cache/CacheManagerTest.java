package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @description CacheManagerのテスト
 */
class CacheManagerTest {

    @Test
    @DisplayName("同一キーでも世代番号が異なれば一意なキャッシュファイルパスを返す")
    void localCacheFile_differentGenerations_returnsUniquePaths() {
        HashCode key = Hashing.sha256().hashString("same-key", StandardCharsets.UTF_8);

        File first = CacheManager.localCacheFile(key, 0L);
        File second = CacheManager.localCacheFile(key, 1L);

        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("キャッシュファイル名はキーのハッシュを接頭辞に持つ")
    void localCacheFile_keepsHashPrefix() {
        HashCode key = Hashing.sha256().hashString("same-key", StandardCharsets.UTF_8);

        File file = CacheManager.localCacheFile(key, 0L);

        assertTrue(file.getName().startsWith(key.toString()));
    }
}
