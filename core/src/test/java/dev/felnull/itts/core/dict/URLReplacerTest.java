package dev.felnull.itts.core.dict;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

public class URLReplacerTest {
    private static final String REPLACED_TEXT = "[URL_SYORYAKU]";

    private static final TestEntry[] ENTRIES = new TestEntry[]{
            new TestEntry("https://www.google.com/", "[URL_SYORYAKU]"),
            new TestEntry("AAA https://www.google.com/ AAA", "AAA [URL_SYORYAKU] AAA"),
            new TestEntry("AAAhttps://www.google.com/aaa", "AAA[URL_SYORYAKU]"),
            new TestEntry("AAAhttps://www.google.com/ AAA", "AAA[URL_SYORYAKU] AAA"),
            new TestEntry("課長壊れる https://www.google.com/ 課長壊れる", "課長壊れる [URL_SYORYAKU] 課長壊れる"),
            new TestEntry("課長壊れるhttps://www.google.com/課長壊れる", "課長壊れる[URL_SYORYAKU]"),
            new TestEntry("課長壊れるhttps://www.google.com/ 課長壊れる", "課長壊れる[URL_SYORYAKU] 課長壊れる"),
            new TestEntry("課長壊れるhttps://www.google.com/　課長壊れる", "課長壊れる[URL_SYORYAKU]　課長壊れる"),
            new TestEntry("課長壊れる　https://www.google.com/　課長壊れる", "課長壊れる　[URL_SYORYAKU]　課長壊れる"),
            new TestEntry("""
                    AAA
                    https://www.google.com/
                    AAA
                    """, """
                    AAA
                    [URL_SYORYAKU]
                    AAA
                    """),
            new TestEntry("""
                    AAA
                    AAA https://www.google.com/ AAA
                    AAA
                    """, """
                    AAA
                    AAA [URL_SYORYAKU] AAA
                    AAA
                    """),
            new TestEntry("""
                    AAA
                    AAAhttps://www.google.com/aaa
                    AAA
                    """, """
                    AAA
                    AAA[URL_SYORYAKU]
                    AAA
                    """),
            new TestEntry("""
                    AAA
                    AAAhttps://www.google.com AAA
                    AAA
                    """, """
                    AAA
                    AAA[URL_SYORYAKU] AAA
                    AAA
                    """),
            new TestEntry("""
                    https://www.google.com
                    AAAhttps://www.google.com AAAhttps://www.google.com
                    AAA
                    """, """
                    [URL_SYORYAKU]
                    AAA[URL_SYORYAKU] AAA[URL_SYORYAKU]
                    AAA
                    """),
            new TestEntry("https://www.google.com/ https://www.google.com/", "[URL_SYORYAKU] [URL_SYORYAKU]"),
            new TestEntry("AAA http://ikisugi.ad.jp/katyou/broken/aiki/#:~:text=野獣先輩 AAA", "AAA [URL_SYORYAKU] AAA"),
            new TestEntry("AAAhttp://ikisugi.ad.jp/katyou/broken/aiki/#:~:text=野獣先輩aaa", "AAA[URL_SYORYAKU]"),
            new TestEntry("AAAhttp://ikisugi.ad.jp/katyou/broken/aiki/#:~:text=野獣先輩 AAA", "AAA[URL_SYORYAKU] AAA"),
            new TestEntry("AAA https://www.google.com/114514 AAA", "AAA [URL_SYORYAKU] AAA"),
            new TestEntry("ftp://ikisugi.tokyo/", "[URL_SYORYAKU]"),
            new TestEntry("AAA ftp://ikisugi.tokyo/ AAA", "AAA [URL_SYORYAKU] AAA"),
            new TestEntry("https://www.google.com", "[URL_SYORYAKU]"),
            new TestEntry("AAA https://www.google.com AAA", "AAA [URL_SYORYAKU] AAA"),
            new TestEntry("http://www.google.com/", "[URL_SYORYAKU]"),
            new TestEntry("AAA http://www.google.com/ AAA", "AAA [URL_SYORYAKU] AAA"),
    };

    @ParameterizedTest
    @MethodSource("entries")
    void testReplace(String before, String after) {
        URLReplacer replacer = new URLReplacer(REPLACED_TEXT);
        String ret = replacer.replace(before);
        Assertions.assertEquals(after, ret);
    }

    private static Stream<Arguments> entries() {
        return Arrays.stream(ENTRIES).map(it -> Arguments.arguments(it.before, it.after));
    }

    private record TestEntry(String before, String after) {
    }
}
