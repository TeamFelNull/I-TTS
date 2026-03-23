package dev.felnull.itts.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正規表現バリデーションユーティリティ
 */
public final class PatternValidator {

    /**
     * ReDoS脆弱性を持つ可能性のあるパターン
     * ネストされた量子 (例: (a+)+, (a*)*) を検出
     */
    private static final Pattern REDOS_PATTERN = Pattern.compile(
            "\\([^)]*[+*][^)]*\\)[+*]|\\([^)]*\\|[^)]*\\)[+*]"
    );

    private PatternValidator() {
    }

    /**
     * 正規表現の構文を検証する
     *
     * @param regex 検証する正規表現
     * @return 検証結果
     */
    @NotNull
    public static ValidationResult validate(@NotNull String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);

            if (isPotentiallyDangerous(regex)) {
                return ValidationResult.failure("この正規表現はパフォーマンス上の問題を引き起こす可能性があります");
            }

            return ValidationResult.success(pattern);
        } catch (PatternSyntaxException e) {
            return ValidationResult.failure("無効な正規表現です: " + e.getDescription());
        }
    }

    /**
     * 正規表現がReDoS脆弱性を持つ可能性があるかチェックする
     *
     * @param regex チェックする正規表現
     * @return 危険な可能性がある場合はtrue
     */
    public static boolean isPotentiallyDangerous(@NotNull String regex) {
        return REDOS_PATTERN.matcher(regex).find();
    }

    /**
     * 正規表現バリデーションの結果
     *
     * @param valid   有効かどうか
     * @param pattern コンパイル済みPattern (有効な場合のみ)
     * @param error   エラーメッセージ (無効な場合のみ)
     */
    public record ValidationResult(boolean valid, Pattern pattern, String error) {

        /**
         * 成功結果を作成
         *
         * @param pattern コンパイル済みPattern
         * @return 成功結果
         */
        public static ValidationResult success(@NotNull Pattern pattern) {
            return new ValidationResult(true, pattern, null);
        }

        /**
         * 失敗結果を作成
         *
         * @param error エラーメッセージ
         * @return 失敗結果
         */
        public static ValidationResult failure(@NotNull String error) {
            return new ValidationResult(false, null, error);
        }

        /**
         * Patternを取得 (成功時のみ)
         *
         * @return コンパイル済みPattern
         */
        public Optional<Pattern> getPattern() {
            return Optional.ofNullable(pattern);
        }
    }
}
