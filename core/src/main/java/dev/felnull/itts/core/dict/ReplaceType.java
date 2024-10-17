package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.util.NameSerializableEnum;

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
}
