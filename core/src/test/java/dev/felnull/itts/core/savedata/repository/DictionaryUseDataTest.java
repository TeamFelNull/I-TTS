package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.savedata.dao.DictionaryUseDataRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DictionaryUseDataTest extends RepoBaseTest {

    @ParameterizedTest
    @MethodSource("dictionaryUseData")
    void testSetAndGet(long serverId, String dictionaryId, DictionaryUseDataRecord dictionaryUseDataRecord) {
        DataRepository repo = createRepository();

        // 初期状態でテスト
        DictionaryUseData dictionaryUseData = repo.getDictionaryUseData(serverId, dictionaryId);
        checkGetAndSet(dictionaryUseData, dictionaryUseDataRecord);

        // キャッシュ参照テスト
        DictionaryUseData dictionaryUseData2 = repo.getDictionaryUseData(serverId, dictionaryId);
        checkGetAndSet(dictionaryUseData2, dictionaryUseDataRecord);
        assertEquals(dictionaryUseData, dictionaryUseData2);

        repo.dispose();

        // 既にレコードが存在して、キャッシュが存在しない状態でテスト
        DataRepository repo2 = createRepository();

        // 初期状態でテスト
        DictionaryUseData dictionaryUseData3 = repo2.getDictionaryUseData(serverId, dictionaryId);
        checkGetAndSet(dictionaryUseData3, dictionaryUseDataRecord);
        assertNotEquals(dictionaryUseData, dictionaryUseData3);

        repo2.dispose();
    }

    private void checkGetAndSet(DictionaryUseData dictionaryUseData, DictionaryUseDataRecord dictionaryUseDataRecord) {
        // データを変更
        dictionaryUseData.setEnable(dictionaryUseDataRecord.enable());
        dictionaryUseData.setPriority(dictionaryUseDataRecord.priority());

        // データを取得
        assertEquals(dictionaryUseDataRecord.enable(), dictionaryUseData.isEnable());
        assertEquals(dictionaryUseDataRecord.priority(), dictionaryUseData.getPriority());
    }

    private static Stream<Arguments> dictionaryUseData() {
        return createTestDataStream(discordServerIdsData().boxed(), dictionaryIdsData(), dictionaryUseDataRecordData())
                .map(data -> Arguments.arguments(data.getLeft(), data.getMiddle(), data.getRight()));
    }
}
