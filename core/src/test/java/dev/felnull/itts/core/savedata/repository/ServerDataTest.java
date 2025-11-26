package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.dao.ServerDataRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ServerDataTest extends RepoBaseTest {

    @ParameterizedTest
    @MethodSource("serverDataTestData")
    void testSetAndGet(long serverId, ServerDataRecord serverDataRecord, String defaultVoiceType, AutoDisconnectMode autoDisconnectMode) {
        DataRepository repo = createRepository();

        // 初期状態でテスト
        ServerData serverData = repo.getServerData(serverId);
        checkGetAndSet(serverData, serverDataRecord, defaultVoiceType, autoDisconnectMode);

        // キャッシュ参照テスト
        ServerData serverData2 = repo.getServerData(serverId);
        checkGetAndSet(serverData2, serverDataRecord, defaultVoiceType, autoDisconnectMode);
        assertEquals(serverData, serverData2);

        repo.dispose();

        // 既にレコードが存在して、キャッシュが存在しない状態でテスト
        DataRepository repo2 = createRepository();

        // 初期状態でテスト
        ServerData serverData3 = repo2.getServerData(serverId);
        checkGetAndSet(serverData3, serverDataRecord, defaultVoiceType, autoDisconnectMode);
        assertNotEquals(serverData, serverData3);

        repo2.dispose();
    }

    private void checkGetAndSet(ServerData serverData, ServerDataRecord serverDataRecord, String defaultVoiceType, AutoDisconnectMode autoDisconnectMode) {
        // データを変更
        serverData.setDefaultVoiceType(defaultVoiceType);
        serverData.setIgnoreRegex(serverDataRecord.ignoreRegex());
        serverData.setNeedJoin(serverDataRecord.needJoin());
        serverData.setOverwriteAloud(serverDataRecord.overwriteAloud());
        serverData.setNotifyMove(serverDataRecord.notifyMove());
        serverData.setReadLimit(serverDataRecord.readLimit());
        serverData.setNameReadLimit(serverDataRecord.nameReadLimit());
        serverData.setAutoDisconnectMode(autoDisconnectMode);

        // データを取得
        assertEquals(defaultVoiceType, serverData.getDefaultVoiceType());
        assertEquals(serverDataRecord.ignoreRegex(), serverData.getIgnoreRegex());
        assertEquals(serverDataRecord.needJoin(), serverData.isNeedJoin());
        assertEquals(serverDataRecord.overwriteAloud(), serverData.isOverwriteAloud());
        assertEquals(serverDataRecord.notifyMove(), serverData.isNotifyMove());
        assertEquals(serverDataRecord.readLimit(), serverData.getReadLimit());
        assertEquals(serverDataRecord.nameReadLimit(), serverData.getNameReadLimit());
        assertEquals(autoDisconnectMode, serverData.getAutoDisconnectMode());
    }

    private static Stream<Arguments> serverDataTestData() {
        return createTestDataStream(discordServerIdsData().boxed(), serverDataRecordData(), voiceTypeNamesNullableData(), autoDisconnectModesData())
                .map(data -> Arguments.arguments(data.getLeft(), data.getLeftCenter(), data.getRightCenter(), data.getRight()));
    }
}
