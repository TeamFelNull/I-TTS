package dev.felnull.itts.core.util;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;

import java.util.OptionalInt;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TestUtils {

    public static <T> void forEachIndexed(Stream<T> stream, IndexedTestConsumer<T> action) {
        MutableInt indexCounter = new MutableInt(0);
        stream.forEach(it -> {
            try {
                action.accept(indexCounter.getAndAdd(1), it);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> void testForEach(Stream<T> stream, TestConsumer<T> action) {
        stream.forEach(it -> {
            try {
                action.accept(it);
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    public static void testForEach(LongStream stream, LongTestConsumer action) {
        stream.forEach(it -> {
            try {
                action.accept(it);
            } catch (Exception e) {
                Assertions.fail(e);
            }
        });
    }

    public static OptionalInt getOptionalIntByInteger(Integer value) {
        if (value != null) {
            return OptionalInt.of(value);
        }
        return OptionalInt.empty();
    }
}
