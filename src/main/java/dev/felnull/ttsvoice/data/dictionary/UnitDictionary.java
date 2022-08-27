package dev.felnull.ttsvoice.data.dictionary;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class UnitDictionary implements Dictionary {
    private static final Pattern UNIT_PREFIX = createPrefixAndUnitPattern();
    private static final Pattern NUMBERS = Pattern.compile("\\d+");

    private static Pattern createPrefixAndUnitPattern() {
        Pattern alphabet = Pattern.compile("[a-z|A-Z]+");
        StringBuilder lastUnits = new StringBuilder();
        StringBuilder preOrUnitMiddle = new StringBuilder();
        for (Unit unit : Unit.values()) {
            String lst = unit.word.substring(unit.word.length() - 1);
            lastUnits.append(lst.toLowerCase(Locale.ROOT)).append(lst.toUpperCase(Locale.ROOT));

            String middle = unit.word.substring(0, unit.word.length() - 1);
            preOrUnitMiddle.append(middle.toLowerCase(Locale.ROOT)).append(middle.toUpperCase(Locale.ROOT));
        }

        for (Prefix prefix : Prefix.values()) {
            if (prefix.word == null || prefix.read == null) continue;
            var mc = alphabet.matcher(prefix.word);
            if (mc.matches()) {
                preOrUnitMiddle.append(prefix.word.toLowerCase(Locale.ROOT)).append(prefix.word.toUpperCase(Locale.ROOT));
            } else {
                preOrUnitMiddle.append(prefix.word);
            }
        }

        return Pattern.compile("\\d+[" + preOrUnitMiddle + "]*[" + lastUnits + "]");
    }

    @Override
    public String replace(String text) {
        text = UNIT_PREFIX.matcher(text).replaceAll(matchResult -> replaceUnitAndPrefix(matchResult.group()));
        return text;
    }

    private String replaceUnitAndPrefix(String text) {
        var nmc = NUMBERS.matcher(text);
        if (!nmc.find()) return text;
        String number = nmc.group();
        String only = text.substring(number.length());
        var unit = Unit.getEndUnit(only);
        if (unit == null) return text;
        String unitStr = only.substring(only.length() - unit.word.length());
        boolean big = Character.isUpperCase(unitStr.charAt(0));
        var prefixStr = only.substring(0, only.length() - unitStr.length());

        if (prefixStr.isEmpty())
            return number + unit.read;

        var prefix = Prefix.getPrefix(prefixStr, unit, big);

        if (prefix == null) return text;

        return number + prefix + unit.read;
    }

    @Override
    public Map<String, String> getEntryShowTexts() {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();

        StringBuilder upsbw = new StringBuilder();
        StringBuilder upsbr = new StringBuilder();
        StringBuilder dosbw = new StringBuilder();
        StringBuilder dosbr = new StringBuilder();

        for (Prefix pre : Prefix.values()) {
            if (pre.getWord() == null || pre.getRead() == null) continue;

            if (pre.isUp()) {
                upsbw.append(pre.getWord()).append(",");
                upsbr.append(pre.getRead()).append(",");
            } else {
                dosbw.append(pre.getWord()).append(",");
                dosbr.append(pre.getRead()).append(",");
            }
        }

        upsbw.deleteCharAt(upsbw.length() - 1);
        upsbr.deleteCharAt(upsbr.length() - 1);
        dosbw.deleteCharAt(dosbw.length() - 1);
        dosbr.deleteCharAt(dosbr.length() - 1);

        builder.put(upsbw.toString(), upsbr.toString());
        builder.put(dosbw.toString(), dosbr.toString());


        return builder.build();
    }

    @Override
    public String getName() {
        return "単位辞書";
    }

    @Override
    public boolean isBuildIn() {
        return true;
    }

    //https://www.mikipulley.co.jp/JP/Services/Tech_data/tech01.html
    private static enum Unit {
        BYTE("b", "ばいと", Prefix.NORMAL_UP),
        METER("m", "めーとる", Prefix.NORMAL_ALL),
        SECOND("s", "びょう", Prefix.NORMAL_DOWN),
        AMPERE("a", "あんぺあ", Prefix.NORMAL_ALL),
        MOLE("mol", "もる", Prefix.NORMAL_ALL),
        CANDELA("cd", "かんでら", Prefix.NORMAL_ALL);

        private final String word;
        private final String read;
        private final Prefix[] prefixes;
        private final Pattern pattern;

        Unit(String word, String read, Prefix... prefixes) {
            this.word = word;
            this.read = read;
            this.prefixes = prefixes;
            this.pattern = Pattern.compile(word);
        }

        public String getWord() {
            return word;
        }

        public String getRead() {
            return read;
        }

        public Prefix[] getPrefixes() {
            return prefixes;
        }

        public boolean isEndMache(String text) {
            text = text.toLowerCase(Locale.ROOT);
            var m = pattern.matcher(text);
            int lstm = -1;
            while (m.find()) {
                lstm = m.end();
            }
            return lstm == text.length();
        }

        public static Unit getEndUnit(String text) {
            for (Unit unit : values()) {
                if (unit.isEndMache(text))
                    return unit;
            }
            return null;
        }
    }

    //https://unit.aist.go.jp/nmij/info/SI_prefixes/index.html
    private static enum Prefix {
        DECI("d", "でし"),
        CENTI("c", "せんち"),
        MILLI("m", "みり"),
        MICRO("μ", "まいくろ"),
        NANO("n", "なの"),
        PICO("p", "ぴこ"),
        FEMTO("f", "ふぇむと"),
        ATTO("a", "あと"),
        ZEPTO("z", "せぷと"),
        YOCTO("y", "よくと"),
        RONTO("r", "ろんと"),
        QUECTO("q", "くえくと"),
        DECA("da", "でか", true),
        HECTO("h", "へくと", true),
        KILO("k", "きろ", true),
        MEGA("m", "めが", true),
        GIGA("g", "ぎが", true),
        TERA("t", "てら", true),
        PETA("p", "ぺた", true),
        EXA("e", "えくさ", true),
        ZETTA("y", "よた", true),
        YOTTA("z", "ぜた", true),
        RONNA("r", "ろな", true),
        QUETTA("q", "くえた", true);

        private static final Prefix[] NORMAL_UP = {Prefix.KILO, Prefix.MEGA, Prefix.GIGA, Prefix.TERA, Prefix.PETA, Prefix.EXA, Prefix.ZETTA, Prefix.YOTTA, Prefix.RONNA, Prefix.QUETTA};
        private static final Prefix[] NORMAL_DOWN = {Prefix.CENTI, Prefix.MILLI, Prefix.MICRO, Prefix.NANO, Prefix.PICO, Prefix.FEMTO, Prefix.ATTO, Prefix.ZEPTO, Prefix.YOCTO, Prefix.RONTO, Prefix.QUECTO};
        private static final Prefix[] NORMAL_ALL = ArrayUtils.addAll(NORMAL_UP, NORMAL_DOWN);

        private final String word;
        private final String read;
        private final boolean up;

        Prefix(String word, String read, boolean up) {
            this.word = word;
            this.read = read;
            this.up = up;
        }

        Prefix(String word, String read) {
            this(word, read, false);
        }

        public String getRead() {
            return read;
        }

        public String getWord() {
            return word;
        }

        public boolean isUp() {
            return up;
        }

        public boolean isMache(String text) {
            return word.equalsIgnoreCase(text);
        }

        public static Prefix getPrefix(String text, Unit unit, boolean big) {
            List<Prefix> prefixes = new ArrayList<>();
            for (Prefix prefix : unit.getPrefixes()) {
                if (prefix.isMache(text))
                    prefixes.add(prefix);
            }
            if (prefixes.isEmpty()) return null;
            if (prefixes.size() == 1) return prefixes.get(0);

            boolean pbig = Character.isUpperCase(text.charAt(0));

            Prefix up = prefixes.stream().filter(Prefix::isUp).findFirst().get();
            Prefix don = prefixes.stream().filter(n -> !n.isUp()).findFirst().get();

            if (!big && pbig)
                return up;
            return don;
        }
    }
}
