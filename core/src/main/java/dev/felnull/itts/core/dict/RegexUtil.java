package dev.felnull.itts.core.dict;

import java.util.*;
import java.util.function.Function;

/**
 * 正規表現関係
 *
 * @author shiro8613
 */
public class RegexUtil {

    /** オプション保持用のリスト */
    private final List<RegexOption> optionList = new ArrayList<>();

    /** 日本語と英語をわけます */
    private List<String> splitJapaneseEnglish(String text) {
        String[] dividedText = text.split("");
        List<String> createdText = new ArrayList<>();
        StringBuilder tmpText = new StringBuilder();
        boolean en = false;

        for (String txt :dividedText) {
            if (txt.matches("[A-Za-z0-9:/#$%&.,-?_]+")) {
                if (!en) {
                    createdText.add(tmpText.toString());
                    tmpText.setLength(0);
                }
                en = true;
                tmpText.append(txt);
            } else {
                if (en) {
                    createdText.add(tmpText.toString());
                    tmpText.setLength(0);
                    en = false;
                }
                tmpText.append(txt);
            }
        }

        if (!tmpText.isEmpty()) {
            createdText.add(tmpText.toString());
        }

        return createdText;
    }

    /**
     * オプション追加
     *
     * @param priority 優先度
     * @param replacedText 置き換えテキスト
     * @param testFunction 置き換え判定用関数
     * @return this このクラスが返ってきます
     */
    public RegexUtil addOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
        this.optionList.add(new RegexOption(priority, replacedText, testFunction));
        return this;
    }

    /**
     * テキストを置き換える
     *
     * @param text 置き換え対象のテキスト
     * @return 置き換え済みテキスト
     */
    public String replaceText(String text) {
        List<String> texts = splitJapaneseEnglish(text);
        List<String> createdText = new ArrayList<>();

        LOOP: for (String txt :texts) {
            for (RegexOption ops :optionList.stream().sorted(Comparator.comparingInt(o -> o.priority)).toList()) {
                if (ops.testFunction.apply(txt)) {
                    createdText.add(ops.replacedText);
                    continue LOOP;
                }
            }
            createdText.add(txt);
        }

        return String.join("", createdText);
    }


    /** オプション定義用クラス
     *
     * @author shiro8613
     * */
    public static class RegexOption {

        /** プライオリティ */
        public int priority;

        /** 判定関数 */
        public Function<String, Boolean> testFunction;

        /**　置き換えテキスト*/
        public String replacedText;

        /**
         * コンストラクタ
         *
         * @param priority 優先度
         * @param replacedText 置き換えテキスト
         * @param testFunction 置き換え判定用関数
         */
        public RegexOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
            this.priority = priority;
            this.replacedText = replacedText;
            this.testFunction = testFunction;
        }
    }

}
