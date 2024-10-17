package dev.felnull.itts.core.savedata.dao;


import java.util.Objects;

/**
 * IDとレコードを保持するクラス
 *
 * @param <T> レコードの型
 */
public final class IdRecordPair<T extends Record> {

    /**
     * ID
     */
    private final int id;

    /**
     * レコード
     */
    private final T record;

    /**
     * コンストラクタ
     *
     * @param id     ID
     * @param record レコード
     */
    public IdRecordPair(int id, T record) {
        this.id = id;
        this.record = record;
    }

    public int getId() {
        return id;
    }

    public T getRecord() {
        return record;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdRecordPair<?> that = (IdRecordPair<?>) o;
        return id == that.id && Objects.equals(record, that.record);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, record);
    }

    @Override
    public String toString() {
        return "IdRecordPair{" + "id=" + id + ", record=" + record + '}';
    }
}
