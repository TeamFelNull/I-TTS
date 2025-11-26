package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.savedata.dao.ServerUserDataRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ServerUserDataTest extends RepoBaseTest {

    @ParameterizedTest
    @MethodSource("serverUserDataTestData")
    void testSetAndGet(long serverId, long userId, ServerUserDataRecord serverUserDataRecord, String voiceType) {
        DataRepository repo = createRepository();

        // 初期状態でテスト
        ServerUserData serverUserData = repo.getServerUserData(serverId, userId);
        checkGetAndSet(serverUserData, serverUserDataRecord, voiceType);

        // キャッシュ参照テスト
        ServerUserData serverUserData2 = repo.getServerUserData(serverId, userId);
        checkGetAndSet(serverUserData2, serverUserDataRecord, voiceType);
        assertEquals(serverUserData, serverUserData2);

        repo.dispose();

        // 既にレコードが存在して、キャッシュが存在しない状態でテスト
        DataRepository repo2 = createRepository();

        // 初期状態でテスト
        ServerUserData serverUserData3 = repo2.getServerUserData(serverId, userId);
        checkGetAndSet(serverUserData3, serverUserDataRecord, voiceType);
        assertNotEquals(serverUserData, serverUserData3);

        repo2.dispose();
    }

    private void checkGetAndSet(ServerUserData serverUserData, ServerUserDataRecord serverUserDataRecord, String voiceType) {
        // データを変更
        serverUserData.setVoiceType(voiceType);
        serverUserData.setDeny(serverUserDataRecord.deny());
        serverUserData.setNickName(serverUserDataRecord.nickName());

        // データを取得
        assertEquals(voiceType, serverUserData.getVoiceType());
        assertEquals(serverUserDataRecord.deny(), serverUserData.isDeny());
        assertEquals(serverUserDataRecord.nickName(), serverUserData.getNickName());
    }

    private static Stream<Arguments> serverUserDataTestData() {
        return createTestDataStream(discordServerIdsData().boxed(), discordUserIdsData().boxed(), serverUserDataRecordData(), voiceTypeNamesNullableData())
                .map(data -> Arguments.arguments(data.getLeft(), data.getLeftCenter(), data.getRightCenter(), data.getRight()));
    }
}
