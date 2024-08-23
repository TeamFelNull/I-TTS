package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.util.NameSerializableEnum;

import java.util.Optional;

public enum AutoDisconnectMode implements NameSerializableEnum {
    OFF("off"),
    ON("on"),
    ON_RECONNECT("on_reconnect");

    private final String name;

    AutoDisconnectMode(String name) {
        this.name = name;
    }

    /**
     * 名前から取得
     *
     * @param name 名前
     * @return 自動切断モード
     */
    public static Optional<AutoDisconnectMode> getByName(String name) {
        return NameSerializableEnum.getByName(AutoDisconnectMode.class, name);
    }

    @Override
    public String getName() {
        return name;
    }
}
