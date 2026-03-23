package dev.felnull.itts.core.dict;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * DictionaryManagerのバリデーションテスト
 */
public class DictionaryManagerTest {

    private final DictionaryManager dictionaryManager = new DictionaryManager();

    @Nested
    @DisplayName("serverDictLoadFromJson()のバリデーションテスト")
    class ServerDictLoadFromJsonTests {

        @Test
        @DisplayName("バージョンが異なる場合は例外をスロー")
        void invalidVersionThrowsException() {
            JsonObject jo = new JsonObject();
            jo.addProperty("version", 999);
            JsonObject entry = new JsonObject();
            entry.addProperty("test", "テスト");
            jo.add("entry", entry);

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertEquals("Unsupported dictionary file version", exception.getMessage());
        }

        @Test
        @DisplayName("versionフィールドがない場合は例外をスロー")
        void missingVersionThrowsException() {
            JsonObject jo = new JsonObject();
            JsonObject entry = new JsonObject();
            entry.addProperty("test", "テスト");
            jo.add("entry", entry);

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertEquals("Unsupported dictionary file version", exception.getMessage());
        }

        @Test
        @DisplayName("entryフィールドがない場合は例外をスロー")
        void missingEntryFieldThrowsException() {
            JsonObject jo = new JsonObject();
            jo.addProperty("version", 0);

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertEquals("Invalid dictionary file format", exception.getMessage());
        }

        @Test
        @DisplayName("entryがオブジェクトでない場合は例外をスロー")
        void entryNotObjectThrowsException() {
            JsonObject jo = new JsonObject();
            jo.addProperty("version", 0);
            jo.addProperty("entry", "not an object");

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertEquals("Invalid dictionary file format", exception.getMessage());
        }

        @Test
        @DisplayName("エントリ数が上限を超える場合は例外をスロー")
        void tooManyEntriesThrowsException() {
            JsonObject jo = new JsonObject();
            jo.addProperty("version", 0);
            JsonObject entry = new JsonObject();

            for (int i = 0; i <= 1000; i++) {
                entry.addProperty("word" + i, "読み" + i);
            }
            jo.add("entry", entry);

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertTrue(exception.getMessage().contains("Dictionary entry count exceeds limit"));
        }

        @ParameterizedTest
        @DisplayName("負のバージョン番号でも例外をスロー")
        @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
        void negativeVersionThrowsException(int version) {
            JsonObject jo = new JsonObject();
            jo.addProperty("version", version);
            JsonObject entry = new JsonObject();
            entry.addProperty("test", "テスト");
            jo.add("entry", entry);

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                    dictionaryManager.serverDictLoadFromJson(jo, 123456L, false));
            Assertions.assertEquals("Unsupported dictionary file version", exception.getMessage());
        }
    }
}
