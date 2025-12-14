package dev.felnull.itts.core.dict;

import java.util.regex.Pattern;

/**
 * URLを置き換える処理
 */
public class URLReplacer {

    /**
     * URL抽出用正規表現
     */
    private static final Pattern URL_REGEX = Pattern.compile("((?:ht|f)tps?://|(?<![\\p{L}0-9_.])www\\.)[-A-Za-z0-9+$&@#/%?=~_|!:,.;]*[^\\s　]*");

    /**
     * 置き換えるテキスト
     */
    private final String replacedText;

    /**
     * コンストラクタ
     *
     * @param replacedText 置き換えるテキスト
     */
    public URLReplacer(String replacedText) {
        this.replacedText = replacedText;
    }

    /**
     * テキスト置き換え
     *
     * @param text テキスト
     * @return 置き換えられたテキスト
     */
    public String replace(String text) {

        if (text.contains("http") | text.contains("ftp")) {
            return URL_REGEX.matcher(text).replaceAll(replacedText);
        }

        return text;
    }
}
