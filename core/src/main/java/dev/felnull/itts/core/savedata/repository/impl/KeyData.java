package dev.felnull.itts.core.savedata.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.felnull.itts.core.savedata.dao.DAO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * キー取得用データ
 *
 * @param <T> キーの型
 */
final class KeyData<T> extends SaveDataBase {

    /**
     * キーのテーブル取得用プロバイダ
     */
    private final Function<DAO, DAO.KeyTable<T>> keyTableProvider;

    /**
     * IDのキャッシュ
     */
    private final LoadingCache<T, Integer> idCache;

    /**
     * キーのキャッシュ
     */
    private final LoadingCache<Integer, T> keyCache;

    KeyData(DataRepositoryImpl repository, Function<DAO, DAO.KeyTable<T>> keyTableProvider, int cacheSize) {
        super(repository);
        this.keyTableProvider = keyTableProvider;

        this.idCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull Integer load(@NotNull T key) {
                        return sqlProcReturnable(connection -> getIdFromDB(connection, key));
                    }
                });

        this.keyCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull T load(@NotNull Integer id) {
                        return sqlProcReturnable(connection -> getKeyFromDB(connection, id));
                    }
                });
    }

    public int getId(@NotNull T key) {
        return idCache.getUnchecked(key);
    }

    public OptionalInt getIdNullable(@Nullable T key) {
        if (key != null) {
            return OptionalInt.of(getId(key));
        } else {
            return OptionalInt.empty();
        }
    }

    @Nullable
    public T getKey(int keyId) {

        if (keyId == 0) {
            return null;
        }

        return keyCache.getUnchecked(keyId);
    }

    private int getIdFromDB(Connection connection, T key) throws SQLException {
        DAO.KeyTable<T> keyTable = keyTableProvider.apply(dao());
        OptionalInt keyId = keyTable.selectId(connection, key);

        if (keyId.isPresent()) {
            // キーが既に存在すれば返す
            return keyId.getAsInt();
        } else {
            // キーが存在しない場合は追加して返す
            keyTable.insertKeyIfNotExists(connection, key);
            return keyTable.selectId(connection, key).orElseThrow();
        }
    }

    private T getKeyFromDB(Connection connection, int keyId) throws SQLException {
        DAO.KeyTable<T> keyTable = keyTableProvider.apply(dao());
        Optional<T> key = keyTable.selectKey(connection, keyId);
        return key.orElseThrow();
    }

}
