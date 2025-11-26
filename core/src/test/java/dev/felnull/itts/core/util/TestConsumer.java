package dev.felnull.itts.core.util;

public interface TestConsumer<T> {
    void accept(T t) throws Exception;
}
