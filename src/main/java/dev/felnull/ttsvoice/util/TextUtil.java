package dev.felnull.ttsvoice.util;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.ibm.icu.text.Transliterator;
import com.mariten.kanatools.KanaConverter;

import java.util.List;

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

    public static String replaceJapaneseToLatin(String text) {
        Tokenizer tokenizer = new Tokenizer();
        List<Token> list = tokenizer.tokenize(text);
        StringBuilder build = new StringBuilder();

        for (Token token : list) {
            String[] splits = token.getAllFeatures().split(",");
            if (splits[7].equals("*")) {
                build.append(token.getSurface());
            } else {
                build.append(splits[7]);
            }
        }
        var ret = build.toString();
        ret = KanaConverter.convertKana(ret, KanaConverter.OP_HAN_KATA_TO_ZEN_HIRA);
        ret = KanaConverter.convertKana(ret, KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA);
        return replaceHiraganaToLatin(ret);
    }


    public static String replaceHiraganaToLatin(String text) {
        Transliterator kataToHira = Transliterator.getInstance("Hiragana-Latin");
        return kataToHira.transliterate(text);
    }
}
