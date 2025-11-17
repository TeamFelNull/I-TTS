package dev.felnull.itts.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * 名前としてシリアライズ可能な列挙型の実装用インターフェイス
 */
public interface NameSerializableEnum {

    /**
     * 名前の文字列から列挙型を取得
     *
     * @param enumClass 列挙型
     * @param name      名前
     * @param defaults  存在しない場合に取得される値
     * @param <T>       列挙型
     * @return 取得された列挙型
     */
    @NotNull
    static <T extends Enum<T> & NameSerializableEnum> T getByName(@NotNull Class<T> enumClass, @NotNull String name, @NotNull T defaults) {
        return getByName(enumClass, name).orElse(defaults);
    }

    /**
     * 名前の文字列から列挙型を取得
     *
     * @param enumClass 列挙型
     * @param name      名前
     * @param <T>       列挙型
     * @return 取得された列挙型
     */
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
