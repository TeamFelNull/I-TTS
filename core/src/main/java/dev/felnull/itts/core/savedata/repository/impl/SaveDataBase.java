package dev.felnull.itts.core.savedata.repository.impl;

import dev.felnull.itts.core.savedata.dao.DAO;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * データインスタンスのベース
 */
abstract class SaveDataBase {

    /**
     * このデータインスタンスと紐づいているレポジトリ
     */
    protected final DataRepositoryImpl repository;

    SaveDataBase(DataRepositoryImpl repository) {
        this.repository = repository;
    }

    protected DAO dao() {
        DAO dao = this.repository.getDAO();

        if (dao == null) {
            IllegalStateException e = new IllegalStateException("The DAO does not exist. The repository may not have been initialized or may have been destroyed.");
            this.repository.fireErrorEvent(e);
            throw e;
        }

        return dao;
    }

    /**
     * SQLアクセス処理実行用
     *
     * @param proc 処理用インタフェース
     */
    protected <T> T sqlProcReturnable(FailableFunction<Connection, T, SQLException> proc) {
        try (Connection con = dao().getConnection()) {
            return proc.apply(con);
        } catch (Throwable throwable) {
            IllegalStateException e = new IllegalStateException("SQL processing failed.", throwable);
            this.repository.fireErrorEvent(e);
            throw e;
        }
    }

    /**
     * SQLアクセス処理実行用
     *
     * @param proc 処理用インタフェース
     */
    protected void sqlProc(FailableConsumer<Connection, SQLException> proc) {
        try (Connection con = dao().getConnection()) {
            proc.accept(con);
        } catch (Throwable throwable) {
            IllegalStateException e = new IllegalStateException("SQL processing failed.", throwable);
            this.repository.fireErrorEvent(e);
            throw e;
        }
    }
}
