package dev.felnull.itts.core.dict;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正規表現関係
 *
 * @author toms0910
 */
public class RegexUtil {

    // ここから下、パターン群

    /**
     * URLの正規表現
     */
    private static final Pattern URL_REGEX = Pattern.compile("^(https?|ftp|file|s?ftp|ssh)://([\\w-]+\\.)+[\\w-]+(/[\\w\\- ./?%&=~#:,]*)?");

    /**
     * ドメインの正規表現
     */
    private static final Pattern DOMAIN_REGEX = Pattern.compile("^([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$");

    /**
     * IPv4の正規表現
     */
    private static final Pattern IPV4_REGEX = Pattern.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})");

    /**
     * IPv6の正規表現
     */
    private static final Pattern IPV6_REGEX = Pattern.compile("((([0-9A-Fa-f]{1,4}:){1,6}:)|(([0-9A-Fa-f]{1,4}:){7}))([0-9A-Fa-f]{1,4})$");

    // ここから下、置き換えテキスト群

    /**
     * URLの置き換えテキスト
     */
    private static final String URL_REPLACE_TEXT = "ユーアールエルショウリャク";

    /**
     * ドメインの置き換えテキスト
     */
    private static final String DOMAIN_REPLACE_TEXT = "ドメインショウリャク";

    /**
     * IPv4の置き換えテキスト
     */
    private static final String IPV4_REPLACE_TEXT = "アイピーブイフォーショウリャク";

    /**
     * IPv6の置き換えテキスト
     */
    private static final String IPV6_REPLACE_TEXT = "アイピーブイロクショウリャク";

    /**
     * パターンと置き換えテキストのマップ
     */
    private final Map<Pattern, String> dictMap = new HashMap<>();

    /**
     * コンストラクタ
     */
    public RegexUtil() {
        //マップに登録
        dictMap.put(URL_REGEX, URL_REPLACE_TEXT);
        dictMap.put(DOMAIN_REGEX, DOMAIN_REPLACE_TEXT);
        dictMap.put(IPV4_REGEX, IPV4_REPLACE_TEXT);
        dictMap.put(IPV6_REGEX, IPV6_REPLACE_TEXT);
    }

    /**
     * テキストを置き換える
     *
     * @param text 置き換え対象のテキスト
     * @return 置き換え済みテキスト
     */
    public String replaceText(String text) {

        //空の文字列用配列
        List<String> returnText = new ArrayList<>();

        //改行コードを空白に変換
        String replaceNewLine2SpaceText = text.replace("\n", " ");
        //空白ごとに分割
        String[] dividedSpaceTexts = replaceNewLine2SpaceText.split(" ");

        //分割されたテキストを一区切りごとに処理を実行
        for (String dividedSpaceText : dividedSpaceTexts) {
            //replacerで一致したテキストを置き換える
            String replacedText = replacer(dividedSpaceText);
            //書き出し用の配列に入れる
            returnText.add(replacedText);
        }

        //配列を一つのテキスト(空白つき)に連結してかえす
        return String.join(" ", returnText);
    }

    private String replacer(String text) {
        //MapをEntrySetに変換
        Set<Entry<Pattern, String>> entrySet = dictMap.entrySet();

        //ループを回して、当てはまるモノが一個でもあったら終了
        for (Entry<Pattern, String> entries : entrySet) {
            //マップから取得したパターンとテキストの比較
            Matcher matcher = entries.getKey().matcher(text);
            if (matcher.find()) { //テキスト内に一致したら
                //置き換え済みテキストをかえす
                return matcher.replaceAll(entries.getValue());
            }
        }

        return text;
    }
}

/*
  使い方
  RegexUtil rgUtil = new RegexUtil();
  String out = rgUtil.replaceText(ここにURLとかが含まれたテキスト);
*/
