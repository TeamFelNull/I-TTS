package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 省略辞書
 *
 * @author MORIMORI0317
 */
public class AbbreviationDictionary implements Dictionary {

    /**
     * コードブロックの正規表現
     */
    private static final Pattern CODE_BLOCK_REGEX = Pattern.compile("```(.|\n)*```");

    /**
     * 正規表現関係
     */
    private final RegexUtil regexUtil = new RegexUtil()
            .addOption(1, "ユーアルエルショウリャク", s -> {
                Pattern pattern = Pattern.compile("https?://[\\w!?/+\\-_~=;.,*&@#$%()'\\[\\]]+");
                Matcher matcher = pattern.matcher(s);
                return matcher.find();
            })
            .addOption(1, "ドメインショウリャク", s -> {
                Pattern pattern = Pattern.compile("^([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$");
                Matcher matcher = pattern.matcher(s);
                return matcher.find();
            })
            .addOption(1, "アイピーブイフォーショウリャク", s -> {
                Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
                Matcher matcher = pattern.matcher(s);
                return matcher.matches();
            })
            .addOption(1, "アイピーブイロクショウリャク", s -> {
                Pattern pattern = Pattern.compile("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,7}:|"
                        + "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|"
                        + "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|"
                        + "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|"
                        + "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|"
                        + ":((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|"
                        + "::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|"
                        + "(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|"
                        + "(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");
                Matcher matcher = pattern.matcher(s);
                return matcher.matches();
            });


    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        text = CODE_BLOCK_REGEX.matcher(text).replaceAll("コードブロックショウリャク");
        return regexUtil.replaceText(text);
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "省略辞書";
    }

    @Override
    public @NotNull String getId() {
        return "abbreviation";
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return ImmutableMap.of("https://...", "URL省略", "``` コードブロック ```", "コードブロック省略");
    }

    @Override
    public int getDefaultPriority() {
        return 1;
    }
}
