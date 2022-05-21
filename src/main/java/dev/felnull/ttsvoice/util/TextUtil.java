package dev.felnull.ttsvoice.util;

import com.ibm.icu.text.Transliterator;

public class TextUtil {
    public static String replaceLatinToHiragana(String text) {
        var kataToHira = Transliterator.getInstance("Latin-Hiragana");
        return kataToHira.transliterate(text);
    }

    public static int getComplementPoint(String target, String text) {
        int point = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            for (int j = 0; j < target.length(); j++) {
                char tc = target.charAt(i);
                if (c == tc)
                    point++;
            }
        }
        return point;
    }
}
