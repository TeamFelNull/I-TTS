package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * 読み上げ辞書
 *
 * @author MORIMORI0317
 */
public interface Dictionary {

    /**
     * テキストに辞書を適用
     *
     * @param text    適用対象テキスト
     * @param guildId サーバーID
     * @return 適用済みテキスト
     */
    @NotNull
    String apply(@NotNull String text, long guildId);

    /**
     * 組み込み辞書かどうか
     *
     * @return 組み込みかどうか
     */
    boolean isBuiltIn();

    /**
     * 辞書名を取得
     *
     * @return 辞書名
     */
    @NotNull
    String getName();

    /**
     * 辞書IDを取得
     *
     * @return 辞書ID
     */
    @NotNull
    String getId();

    /**
     * 辞書情報表示に使用される置き換え対象と置き換え後の文字
     *
     * @param guildId サーバーID
     * @return 置き換え対象と置き換え後の文字が含まれたMAP
     */
    @NotNull
    @Unmodifiable
    Map<String, String> getShowInfo(long guildId);

    /**
     * 初期状態の優先度を取得
     *
     * @return 初期状態の優先度
     */
    int getDefaultPriority();
}
