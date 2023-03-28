package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class UnitDictionary implements Dictionary {

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        return UNIT_PREFIX.matcher(text).replaceAll(matchResult -> {
            String lst = null;
            if (matchResult.end() < text.length())
                lst = String.valueOf(text.charAt(matchResult.end()));
            return replaceUnitAndPrefix(matchResult.group(), lst);
        });
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "単位辞書";
    }

    @Override
    public @NotNull String getId() {
        return "unit";
    }

    private static final Pattern UNIT_PREFIX = createPrefixAndUnitPattern();
    private static final Pattern NUMBERS = Pattern.compile("\\d+");

    private static Pattern createPrefixAndUnitPattern() {
        Pattern alphabet = Pattern.compile("[a-zA-Z]+");
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

    private String replaceUnitAndPrefix(String text, String after) {
        Pattern alphabets = Pattern.compile("[a-zA-Z-]+");
        if (after != null && alphabets.matcher(after).matches())
            return text;

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

        return number + prefix.read + unit.read;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
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

        StringBuilder unitsbw = new StringBuilder();
        StringBuilder unitsbr = new StringBuilder();

        for (Unit unit : Unit.values()) {
            unitsbw.append(unit.getWord()).append(",");
            unitsbr.append(unit.getRead()).append(",");
        }

        unitsbw.deleteCharAt(unitsbw.length() - 1);
        unitsbr.deleteCharAt(unitsbr.length() - 1);

        builder.put(unitsbw.toString(), unitsbr.toString());

        return builder.build();
    }

    @Override
    public int getDefaultPriority() {
        return -1;
    }

    //https://www.mikipulley.co.jp/JP/Services/Tech_data/tech01.html
    private static enum Unit {
        GRAM("g", "ぐらむ", Prefix.NORMAL_ALL),
        BYTE("b", "ばいと", Prefix.NORMAL_UP),
        METER("m", "めーとる", Prefix.NORMAL_ALL),
        SECOND("s", "秒", Prefix.NORMAL_DOWN),
        AMPERE("a", "あんぺあ", Prefix.NORMAL_ALL),
        MOLE("mol", "もる", Prefix.NORMAL_ALL),
        CANDELA("cd", "かんでら", Prefix.NORMAL_ALL),
        RADIAN("rad", "らじあん", Prefix.NORMAL_ALL),
        STERADIAN("sr", "すてらじあん", Prefix.NORMAL_ALL),
        HERTZ("Hz", "すてらじあん", ArrayUtils.addAll(Prefix.NORMAL_ALL, Prefix.DECA, Prefix.HECTO)),
        NEWTON("n", "にゅーとん", Prefix.NORMAL_ALL),
        PASCAL("Pa", "ぱすかる", Prefix.NORMAL_ALL),
        JOULE("J", "じゅーる", Prefix.NORMAL_ALL),
        WATT("W", "わっと", Prefix.NORMAL_ALL),
        COULOMB("C", "くーろん", Prefix.NORMAL_ALL),
        VOLT("V", "ぼると", Prefix.NORMAL_ALL),
        FARAD("F", "ふぁらど", Prefix.NORMAL_ALL),
        OHM("Ω", "おーむ", Prefix.NORMAL_ALL),
        SIEMENS("S", "じーめんす", Prefix.NORMAL_ALL),
        WEBER("Wb", "うぇーば", Prefix.NORMAL_ALL),
        TESLA("T", "てすら", Prefix.NORMAL_ALL),
        HENRY("H", "へんりー", Prefix.NORMAL_ALL),
        CELSIUS("°C", "ど"),
        LUMEN("lm", "るーめん", Prefix.NORMAL_ALL),
        LUX("lx", "るくす", Prefix.NORMAL_ALL),
        BECQUEREL("Bq", "べくれる", Prefix.NORMAL_ALL),
        GRAY("Gy", "ぐれい", Prefix.NORMAL_ALL),
        SIEVERT("Sv", "しーべると", Prefix.NORMAL_ALL),
        KATAL("kat", "かたーる", Prefix.NORMAL_ALL),
        KELVIN("k", "けるびん"),
        TONS("t", "とん"),
        LITER("l", "りっとる", ArrayUtils.addAll(Prefix.NORMAL_ALL, Prefix.DECA, Prefix.DECI));

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
            if (findPrefix(text))
                return true;
            Pattern alphabet = Pattern.compile("[a-zA-Z]+");

            text = alphabet.matcher(text).replaceAll(n -> n.group().toLowerCase(Locale.ROOT));
            if (findPrefix(text))
                return true;

            text = alphabet.matcher(text).replaceAll(n -> n.group().toUpperCase(Locale.ROOT));

            return findPrefix(text);
        }

        private boolean findPrefix(String text) {
            var m = pattern.matcher(text);
            int lstm = -1;
            while (m.find()) {
                lstm = m.end();
            }
            return lstm == text.length();
        }

        public static Unit getEndUnit(String text) {
            List<Unit> maches = new ArrayList<>();

            for (Unit unit : values()) {
                if (unit.isEndMache(text))
                    maches.add(unit);
            }

            if (maches.isEmpty()) return null;
            if (maches.size() == 1) return maches.get(0);

            for (Unit unit : maches) {
                if (unit.word.equals(text))
                    return unit;
            }

            return maches.get(0);
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
