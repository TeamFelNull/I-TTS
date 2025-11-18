package dev.felnull.itts.core.discord;

import dev.felnull.itts.core.util.NameSerializableEnum;

import java.util.Optional;

/**
 * 自動切断モードの種類
 */
public enum AutoDisconnectMode implements NameSerializableEnum {
    OFF("off"),
    ON("on"),
    ON_RECONNECT("on_reconnect");

    /**
     * 名前
     */
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

    /**
     * 自動切断は有効かどうか
     *
     * @return 有効であればtrue
     */
    public boolean isOn() {
        return this == ON || this == ON_RECONNECT;
    }


    /**
     * 再接続を行うかどうか
     *
     * @return 再接続を行う場合はtrue
     */
    public boolean isReconnect() {
        return this == ON_RECONNECT;
    }
}
