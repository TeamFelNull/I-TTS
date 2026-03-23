package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSRuntimeUse;
import java.util.regex.Pattern;

/**
 * ドメインを置き換える処理
 */
public class DomainReplacer implements ITTSRuntimeUse {

    /**
     * 置き換えるテキスト
     */
    private final String replacedText;

    /**
     * コンストラクタ
     *
     * @param replacedText 置き換えるテキスト
     */
    public DomainReplacer(String replacedText) {
        this.replacedText = replacedText;
    }

    /**
     * テキスト置き換え
     *
     * @param text テキスト
     * @return 置き換えられたテキスト
     */
    public String replace(String text) {
        Pattern domainPattern = getDomainListManager().getPattern();
        if (domainPattern != null) {
            return domainPattern.matcher(text).replaceAll(replacedText);
        }

        return text;
    }
}
