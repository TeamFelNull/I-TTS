package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.util.NameSerializableEnum;

public enum ReplaceType implements NameSerializableEnum {
    CHARACTER("character"),
    WORD("word");

    private final String name;

    ReplaceType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
