package dev.felnull.itts.core;

import org.jetbrains.annotations.NotNull;

/**
 * 実行環境の情報
 *
 * @param developmentEnvironment 開発環境かどうか
 * @param version                バージョン
 */
public record RuntimeInfo(boolean developmentEnvironment, @NotNull String version) {
}
