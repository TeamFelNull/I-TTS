package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.util.NameSerializableEnum;

import java.util.Optional;

/**
 * 辞書置き換えタイプ
 */
public enum ReplaceType implements NameSerializableEnum {
    CHARACTER("character"),
    WORD("word");

    /**
     * 名前
     */
    private final String name;

    ReplaceType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 名前から取得
     *
     * @param name 名前
     * @return 自動切断モード
     */
    public static Optional<ReplaceType> getByName(String name) {
        return NameSerializableEnum.getByName(ReplaceType.class, name);
    }
}
