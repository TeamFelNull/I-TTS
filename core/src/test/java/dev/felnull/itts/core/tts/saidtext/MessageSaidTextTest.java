package dev.felnull.itts.core.tts.saidtext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageSaidTextTest {

    @ParameterizedTest
    @MethodSource("spoilerTexts")
    void replaceSpoilers_replacesSpoilerContents(String text, String expected) {
        assertEquals(expected, MessageSaidText.replaceSpoilers(text));
    }

    private static Stream<Arguments> spoilerTexts() {
        return Stream.of(
                Arguments.of("通常のメッセージ", "通常のメッセージ"),
                Arguments.of("前 ||秘密|| 後", "前 スポイラー省略 後"),
                Arguments.of("||一つ目||と||二つ目||", "スポイラー省略とスポイラー省略"),
                Arguments.of("||複数\n行||", "スポイラー省略"),
                Arguments.of("||||", "スポイラー省略"),
                Arguments.of("||閉じられていない", "||閉じられていない")
        );
    }
}
