package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import dev.felnull.itts.core.dict.ReplaceType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ServerCustomDictionaryDataTest extends RepoBaseTest {

    @ParameterizedTest
    @MethodSource("serverCustomDictionaryData")
    void testAddAndGetRemove(long serverId, Map<String, String> dictData, ReplaceType replaceType) {
        DataRepository repo = createRepository();

        // 初期状態でテスト
        CustomDictionaryData customDictionaryData = repo.getServerCustomDictionaryData(serverId);
        checkAddAndGetRemove(customDictionaryData, dictData, replaceType);

        // キャッシュ参照テスト
        CustomDictionaryData customDictionaryData2 = repo.getServerCustomDictionaryData(serverId);
        checkAddAndGetRemove(customDictionaryData2, dictData, replaceType);
        assertEquals(customDictionaryData, customDictionaryData2);

        repo.dispose();

        // 既にレコードが存在して、キャッシュが存在しない状態でテスト
        DataRepository repo2 = createRepository();

        // 初期状態でテスト
        CustomDictionaryData customDictionaryData3 = repo2.getServerCustomDictionaryData(serverId);
        checkAddAndGetRemove(customDictionaryData3, dictData, replaceType);
        assertNotEquals(customDictionaryData, customDictionaryData3);

        repo2.dispose();
    }

    private void checkAddAndGetRemove(CustomDictionaryData customDictionaryData, Map<String, String> dictData, ReplaceType replaceType) {
        assertTrue(customDictionaryData.getAll().isEmpty());

        // 辞書エントリを追加
        dictData.forEach((target, read) -> customDictionaryData.add(new CustomDictionaryEntry(target, read, replaceType)));

        // 辞書エントリを取得して確認
        List<IdCustomDictionaryEntryPair> allEntries = customDictionaryData.getAll();
        Set<Pair<String, String>> retDataSet = new HashSet<>();
        allEntries.forEach(data -> {
            assertEquals(replaceType, data.entry().replaceType());
            retDataSet.add(Pair.of(data.entry().target(), data.entry().read()));
        });

        Set<Pair<String, String>> dictDataSet = new HashSet<>();
        dictData.forEach((target, read) -> dictDataSet.add(Pair.of(target, read)));

        assertTrue(CollectionUtils.isEqualCollection(retDataSet, dictDataSet));

        // 辞書エントリを削除
        allEntries.forEach(it -> customDictionaryData.remove(it.id()));

        assertTrue(customDictionaryData.getAll().isEmpty());
    }

    private static Stream<Arguments> serverCustomDictionaryData() {
        return createTestDataStream(discordServerIdsData().boxed(), Stream.of(customDictionaryData1().toList(), customDictionaryData2().toList()), dictionaryReplaceTypesData())
                .map(data -> {
                    Map<String, String> dictData = new HashMap<>();
                    data.getMiddle().forEach(it -> dictData.put(it.getLeft(), it.getRight()));
                    return Arguments.arguments(data.getLeft(), dictData, data.getRight());
                });
    }

    @Test
    void testGetByTarget() {
        DataRepository repo = createRepository();
        CustomDictionaryData customDictionaryData = repo.getServerCustomDictionaryData(114514);

        assertTrue(customDictionaryData.getAll().isEmpty());

        // エントリ追加
        customDictionaryData.add(new CustomDictionaryEntry("kbtit", "KBTITさん", ReplaceType.CHARACTER));
        customDictionaryData.add(new CustomDictionaryEntry("太い", "お太い", ReplaceType.CHARACTER));
        customDictionaryData.add(new CustomDictionaryEntry("kbtit", "くぼたいと", ReplaceType.WORD));
        customDictionaryData.add(new CustomDictionaryEntry("aikiso", "あいきそ", ReplaceType.WORD));
        customDictionaryData.add(new CustomDictionaryEntry("test", "てすと", ReplaceType.CHARACTER));
        customDictionaryData.add(new CustomDictionaryEntry("許して", "お兄さん許して", ReplaceType.CHARACTER));

        // 読みから取得を確認
        List<IdCustomDictionaryEntryPair> ret = customDictionaryData.getByTarget("kbtit");
        assertEquals(2, ret.size());
        assertEquals("kbtit", ret.get(0).entry().target());
        assertEquals("kbtit", ret.get(1).entry().target());

        // 辞書エントリを削除
        customDictionaryData.getAll().forEach(it -> customDictionaryData.remove(it.id()));
        assertTrue(customDictionaryData.getAll().isEmpty());

        repo.dispose();
    }
}
