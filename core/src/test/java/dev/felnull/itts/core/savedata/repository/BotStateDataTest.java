package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.tts.TTSChannelPair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BotStateDataTest extends RepoBaseTest {

    @ParameterizedTest
    @MethodSource("botStateData")
    void testSetAndGet(long serverId, long botId, Pair<TTSChannelPair, TTSChannelPair> botStatePair) {
        DataRepository repo = createRepository();

        // 初期状態でテスト
        BotStateData botStateData = repo.getBotStateData(serverId, botId);
        checkGetAndSet(botStateData, botStatePair.getLeft(), botStatePair.getRight());

        // キャッシュ参照テスト
        BotStateData botStateData2 = repo.getBotStateData(serverId, botId);
        checkGetAndSet(botStateData2, botStatePair.getLeft(), botStatePair.getRight());
        assertEquals(botStateData, botStateData2);

        repo.dispose();

        // 既にレコードが存在して、キャッシュが存在しない状態でテスト
        DataRepository repo2 = createRepository();

        // 初期状態でテスト
        BotStateData botStateData3 = repo2.getBotStateData(serverId, botId);
        checkGetAndSet(botStateData3, botStatePair.getLeft(), botStatePair.getRight());
        assertNotEquals(botStateData, botStateData3);

        repo2.dispose();
    }

    private void checkGetAndSet(BotStateData botStateData, TTSChannelPair connectedChannel, TTSChannelPair reconnectChannel) {
        // 単体
        if (connectedChannel != null) {
            botStateData.setSpeakAudioChannel(connectedChannel.speakAudioChannel());
            botStateData.setReadAroundTextChannel(connectedChannel.readTextChannel());

            assertEquals(connectedChannel, botStateData.getConnectedChannelPair());
            assertEquals(connectedChannel.speakAudioChannel(), botStateData.getSpeakAudioChannel());
            assertEquals(connectedChannel.readTextChannel(), botStateData.getReadAroundTextChannel());

            botStateData.setSpeakAudioChannel(connectedChannel.speakAudioChannel() + 1);
            botStateData.setReadAroundTextChannel(connectedChannel.readTextChannel() + 1);
        }

        // ペア
        botStateData.setConnectedChannelPair(connectedChannel);
        botStateData.setReconnectChannelPair(reconnectChannel);

        assertEquals(connectedChannel, botStateData.getConnectedChannelPair());
        assertEquals(reconnectChannel, botStateData.getReconnectChannelPair());

        if (connectedChannel != null) {
            assertEquals(connectedChannel.speakAudioChannel(), botStateData.getSpeakAudioChannel());
            assertEquals(connectedChannel.readTextChannel(), botStateData.getReadAroundTextChannel());
        } else {
            assertNull(botStateData.getSpeakAudioChannel());
            assertNull(botStateData.getReadAroundTextChannel());
        }
    }

    private static Stream<Arguments> botStateData() {
        return createTestDataStream(discordServerIdsData().boxed(), discordBotIdsData().boxed(), botStatePairsData())
                .map(data -> Arguments.arguments(data.getLeft(), data.getMiddle(), data.getRight()));
    }
}
