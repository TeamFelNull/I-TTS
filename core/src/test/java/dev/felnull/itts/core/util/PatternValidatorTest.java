package dev.felnull.itts.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

/**
 * PatternValidatorのテストクラス
 */
public class PatternValidatorTest {

    @Nested
    @DisplayName("validate()メソッドのテスト")
    class ValidateTests {

        @ParameterizedTest
        @DisplayName("有効な正規表現が成功を返す")
        @ValueSource(strings = {
                "^[a-z]+$",
                "[0-9]{3}-[0-9]{4}",
                "hello",
                "\\d+",
                "a*b+c?",
                "^(foo|bar)$",
                "[A-Za-z0-9_]+",
                "\\w+@\\w+\\.\\w+",
                "(?:abc)+",
                "\\p{L}+"
        })
        void validPatternsReturnSuccess(String regex) {
            PatternValidator.ValidationResult result = PatternValidator.validate(regex);
            Assertions.assertTrue(result.valid());
            Assertions.assertNotNull(result.pattern());
            Assertions.assertNull(result.error());
            Assertions.assertTrue(result.getPattern().isPresent());
        }

        @ParameterizedTest
        @DisplayName("無効な構文がエラーを返す")
        @MethodSource("invalidPatterns")
        void invalidSyntaxReturnsError(String regex, String expectedErrorContains) {
            PatternValidator.ValidationResult result = PatternValidator.validate(regex);
            Assertions.assertFalse(result.valid());
            Assertions.assertNull(result.pattern());
            Assertions.assertNotNull(result.error());
            Assertions.assertTrue(result.error().contains("無効な正規表現です"));
            Assertions.assertTrue(result.getPattern().isEmpty());
        }

        private static Stream<Arguments> invalidPatterns() {
            return Stream.of(
                    Arguments.of("[a-z", "Unclosed character class"),
                    Arguments.of("(abc", "Unclosed group"),
                    Arguments.of("(?P<name>", "Unknown inline modifier"),
                    Arguments.of("*abc", "Dangling meta character"),
                    Arguments.of("+abc", "Dangling meta character"),
                    Arguments.of("?abc", "Dangling meta character"),
                    Arguments.of("\\", "Unexpected internal error"),
                    Arguments.of("[z-a]", "Illegal character range")
            );
        }

        @ParameterizedTest
        @DisplayName("ReDoS脆弱性パターンがエラーを返す")
        @ValueSource(strings = {
                "(a+)+",
                "(a*)*",
                "(a+)*",
                "(a*)+",
                "(x+)+b",
                "(a|b)+",
                "(a|b)*",
                "(x|y)+z"
        })
        void redosPatternsReturnError(String regex) {
            PatternValidator.ValidationResult result = PatternValidator.validate(regex);
            Assertions.assertFalse(result.valid());
            Assertions.assertNull(result.pattern());
            Assertions.assertNotNull(result.error());
            Assertions.assertTrue(result.error().contains("パフォーマンス上の問題"));
        }
    }

    @Nested
    @DisplayName("isPotentiallyDangerous()メソッドのテスト")
    class IsPotentiallyDangerousTests {

        @ParameterizedTest
        @DisplayName("危険なパターンを検出する")
        @ValueSource(strings = {
                "(a+)+",
                "(a*)*",
                "(a+)*",
                "(a*)+",
                "(x+)+b",
                "(a|b)+",
                "(a|b)*",
                "(foo|bar)*"
        })
        void detectsDangerousPatterns(String regex) {
            Assertions.assertTrue(PatternValidator.isPotentiallyDangerous(regex));
        }

        @ParameterizedTest
        @DisplayName("安全なパターンは検出しない")
        @ValueSource(strings = {
                "^[a-z]+$",
                "[0-9]{3}-[0-9]{4}",
                "hello",
                "\\d+",
                "a+b+c+",
                "^(foo|bar)$",
                "[A-Za-z0-9_]+",
                "\\w+@\\w+\\.\\w+",
                "(?:abc)",
                "a{1,10}"
        })
        void safePatternNotDetected(String regex) {
            Assertions.assertFalse(PatternValidator.isPotentiallyDangerous(regex));
        }
    }

    @Nested
    @DisplayName("ValidationResultのテスト")
    class ValidationResultTests {

        @ParameterizedTest
        @DisplayName("成功結果が正しく作成される")
        @ValueSource(strings = {"abc", "\\d+", "[a-z]+"})
        void successResultCreatedCorrectly(String regex) {
            PatternValidator.ValidationResult result = PatternValidator.ValidationResult.success(
                    java.util.regex.Pattern.compile(regex)
            );
            Assertions.assertTrue(result.valid());
            Assertions.assertNotNull(result.pattern());
            Assertions.assertNull(result.error());
            Assertions.assertEquals(regex, result.pattern().pattern());
        }

        @ParameterizedTest
        @DisplayName("失敗結果が正しく作成される")
        @ValueSource(strings = {"エラー1", "エラー2", "無効な正規表現です"})
        void failureResultCreatedCorrectly(String errorMessage) {
            PatternValidator.ValidationResult result = PatternValidator.ValidationResult.failure(errorMessage);
            Assertions.assertFalse(result.valid());
            Assertions.assertNull(result.pattern());
            Assertions.assertEquals(errorMessage, result.error());
            Assertions.assertTrue(result.getPattern().isEmpty());
        }
    }
}
