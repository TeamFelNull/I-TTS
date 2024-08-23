package dev.felnull.itts.core.savedata.impl;

import org.apache.commons.lang3.function.FailableFunction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * セーブデータのベース
 */
abstract class SaveDataBase {

    /**
     * DAO
     */
    protected final DAO dao;

    private final ErrorCounter errorCounter;

    /**
     * コンストラクタ
     *
     * @param dao DAO
     */
    protected SaveDataBase(DAO dao, ErrorCounter errorCounter) {
        this.dao = dao;
        this.errorCounter = errorCounter;
    }

    /**
     * 初期化
     */
    protected void init() {
        try (Connection con = dao.getConnection()) {
            initRecord(con);
        } catch (SQLException ex) {
            throw new IllegalStateException("Record initialization failure", ex);
        }
    }

    /**
     * レコード関係の初期化
     *
     * @param connection コネクション
     * @throws SQLException エラー
     */
    abstract protected void initRecord(Connection connection) throws SQLException;

    /**
     * SQLへのアクセス処理実行用
     *
     * @param consumer 処理用インタフェース
     */
    protected <T> T sqlProcWrap(FailableFunction<Connection, T, SQLException> consumer) {
        try (Connection con = dao.getConnection()) {
            return consumer.apply(con);
        } catch (Throwable throwable) {
            errorCounter.inc();
            throw new IllegalStateException("SQL processing failed.", throwable);
        }
    }
}
