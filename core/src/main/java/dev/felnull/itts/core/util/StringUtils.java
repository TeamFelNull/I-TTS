package dev.felnull.itts.core.util;

/**
 * 文字列関係のユーティリティ
 *
 * @author MORIMORI0317
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * 補完の点数を求める<br/>
     * 予測補完を行う際、対象のテキストに対して、どれぐらい近いテキストなのか求めるために使用
     *
     * @param target 対象のテキスト
     * @param text   比較を行うテキスト
     * @return 点数
     */
    public static int getComplementPoint(String target, String text) {
        target = target.toLowerCase();
        text = text.toLowerCase();

        int point = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            for (int j = 0; j < target.length(); j++) {
                char tc = target.charAt(j);
                if (c == tc) {
                    point++;
                }
            }
        }
        return point;
    }
}
