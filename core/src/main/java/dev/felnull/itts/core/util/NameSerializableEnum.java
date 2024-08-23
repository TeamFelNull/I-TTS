package dev.felnull.itts.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * 名前としてシリアライズ可能な列挙型の実装用インターフェイス
 */
public interface NameSerializableEnum {

    @NotNull
    static <T extends Enum<T> & NameSerializableEnum> T getByName(@NotNull Class<T> enumClass, @NotNull String name, @NotNull T defaults) {
        return getByName(enumClass, name).orElse(defaults);
    }

    @NotNull
    static <T extends Enum<T> & NameSerializableEnum> Optional<T> getByName(@NotNull Class<T> enumClass, @NotNull String name) {
        T[] values = enumClass.getEnumConstants();
        return Arrays.stream(values)
                .filter(it -> it.getName().equals(name))
                .findFirst();
    }

    /**
     * シリアライズに利用する名前<br/>
     * 必ず一意にしてください。
     *
     * @return 名前
     */
    String getName();
}
