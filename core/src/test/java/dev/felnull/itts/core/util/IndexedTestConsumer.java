package dev.felnull.itts.core.util;

public interface IndexedTestConsumer<T> {
    void accept(int index, T t) throws Exception;
}
