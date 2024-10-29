package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.DAO;
import dev.felnull.itts.core.savedata.dao.IdRecordPair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * レコードごとのデータ
 *
 * @param <K> キーの型
 * @param <T> レコードの型
 */
abstract class RecordData<K extends Record, T extends Record> extends SaveDataBase {

    /**
     * レコードID
     */
    private int recordId = -1;

    RecordData(DataRepositoryImpl repository) {
        super(repository);
    }

    void init() {
        try (Connection con = dao().getConnection()) {
            initRecord(con, getInitRecordContext());
        } catch (Throwable throwable) {
            this.repository.fireErrorEvent();
            throw new IllegalStateException("Record initialization failure", throwable);
        }
    }

    private void initRecord(Connection connection, InitRecordContext context) throws SQLException {
        DAO.DataTable<K, T> table = context.getTable();
        K key = context.getKey();

        Optional<IdRecordPair<T>> record = table.selectRecordByKey(connection, key);

        // レコードが既に存在すればIDを代入する
        if (record.isPresent()) {
            recordId = record.get().getId();
            return;
        }

        // レコードが存在しなければ、追加後にIDを取得して代入する
        T initialRecord = context.getInitialRecordProvider().get();

        table.insertRecordIfNotExists(connection, key, initialRecord);

        recordId = table.selectRecordByKey(connection, key).orElseThrow().getId();
    }

    protected abstract InitRecordContext getInitRecordContext();

    protected int recordId() {
        return recordId;
    }

    /**
     * レコードを初期化するために必要なコンテキスト
     */
    protected class InitRecordContext {

        /**
         * テーブル
         */
        private final DAO.DataTable<K, T> table;

        /**
         * キー
         */
        private final K key;

        /**
         * 初期状態のレコードを取得するためのプロバイダ
         */
        private final Supplier<T> initialRecordProvider;

        protected InitRecordContext(DAO.DataTable<K, T> table, K key, Supplier<T> initialRecordProvider) {
            this.table = table;
            this.key = key;
            this.initialRecordProvider = initialRecordProvider;
        }

        public DAO.DataTable<K, T> getTable() {
            return table;
        }

        public K getKey() {
            return key;
        }

        public Supplier<T> getInitialRecordProvider() {
            return initialRecordProvider;
        }
    }
}
