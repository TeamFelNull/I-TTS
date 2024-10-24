package dev.felnull.itts.core.savedata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.felnull.fnjl.tuple.FNQuadruple;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import dev.felnull.itts.core.savedata.dao.DictionaryUseDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerDataRecord;
import dev.felnull.itts.core.savedata.dao.ServerUserDataRecord;
import dev.felnull.itts.core.tts.TTSChannelPair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class AbstractSaveDataTest {

    private static final long[] DISCORD_COMMON_IDS = {
            0L,
            1L,
            1919L,
            0x114514L,
            Long.MAX_VALUE
    };

    private static final long[] DISCORD_SERVER_IDS = {
            600929948529590272L,
            436404936151007241L,
            1020960278650835034L,
            1219628077353668688L,
            930083398691733565L
    };

    private static final long[] DISCORD_USER_IDS = {
            328520268274204673L,
            419032985476530176L,
            317190690331295752L,
            977496014854447114L,
            703939939796123648L
    };

    private static final long[] DISCORD_CHANNEL_IDS = {
            608914022883917824L,
            607133287281983528L,
            1263830533083762709L,
            942790849723523092L,
            1264947689628373185L
    };

    private static final long[] DISCORD_BOT_IDS = {
            1031199180896620605L,
            993040381677666384L,
            1017318323488292924L,
            1069986309206188125L,
            802205328510156821L
    };

    private static final String[] DICTIONARY_IDS = {
            "abbreviation",
            "global",
            "romaji",
            "server",
            "unit"
    };

    private static final String[] VOICE_TYPE_NAMES = {
            "yajusenpai",
            "katyou",
            "kbtit",
            "nktidksg",
            "akys",
    };

    private static final Map<String, String> CUSTOM_DICTIONARY_ENTRIES_1 = ImmutableMap.of(
            "```(.|\n)*```", "コードブロック省略",
            "test", "てすと",
            "kbtit", "くぼたいと",
            "盛り", "さかり",
            "県北", "けんぼく",
            "糞", "くそ",
            "土方", "どかた",
            "悪目立ち", "わるめだち",
            "paypay", "ぺいぺい",
            "滑舌", "かつぜつ"
    );

    private static final Map<String, String> CUSTOM_DICTIONARY_ENTRIES_2 = ImmutableMap.of(
            "bb", "ぶるーばっく",
            "草", "くさ",
            "dead", "でっど",
            "194", "いくよ",
            "ton", "遠野",
            "114", "いいよ",
            "pysm", "ぽやしみ",
            "810", "野獣先輩",
            "514", "こいよ",
            "壊れる", "こわれちゃ＾～う"
    );

    private static LongStream discordCommonIdsData() {
        return Arrays.stream(DISCORD_COMMON_IDS)
                .flatMap(it -> LongStream.of(it, -it))
                .distinct();
    }

    private static final ServerDataRecord[] SERVER_DATA_RECORDS = new ServerDataRecord[]{
            new ServerDataRecord(null, null, true, true, false, 0, 15, 0),
            new ServerDataRecord(null, "", false, true, true, 100, 0, 0),
            new ServerDataRecord(null, null, false, false, false, 364, 12, 0),
            new ServerDataRecord(null, "(;).*", true, false, false, 130, 1, 0),
            new ServerDataRecord(null, "(!|/|\\\\$|`).*", false, true, false, Integer.MAX_VALUE, 5, 0),
            new ServerDataRecord(null, "ikisugi", false, false, true, 30, Integer.MAX_VALUE, 0),
            new ServerDataRecord(null, "F.C.O.H", true, true, true, 1, 40, 0)
    };

    private static final ServerUserDataRecord[] SERVER_USER_DATA_RECORDS = new ServerUserDataRecord[]{
            new ServerUserDataRecord(null, false, null),
            new ServerUserDataRecord(null, true, null),
            new ServerUserDataRecord(null, false, "野獣先輩"),
            new ServerUserDataRecord(null, true, "NKTIDKSG")
    };

    private static final DictionaryUseDataRecord[] DICTIONARY_USE_DATA_RECORDS = new DictionaryUseDataRecord[]{
            new DictionaryUseDataRecord(null, 0),
            new DictionaryUseDataRecord(true, 1),
            new DictionaryUseDataRecord(false, -1),
            new DictionaryUseDataRecord(false, null),
    };

    private static final List<Pair<TTSChannelPair, TTSChannelPair>> BOT_STATE_DATA_PAIR = ImmutableList.of(
            Pair.of(null, null),
            Pair.of(new TTSChannelPair(1919L, 810L), null),
            Pair.of(null, new TTSChannelPair(364364L, 114514L)),
            Pair.of(new TTSChannelPair(1919L, 810L), new TTSChannelPair(364364L, 114514L))
    );

    protected static LongStream discordServerIdsData() {
        return Streams.concat(discordCommonIdsData(), Arrays.stream(DISCORD_SERVER_IDS));
    }

    protected static LongStream discordUserIdsData() {
        return Streams.concat(discordCommonIdsData(), Arrays.stream(DISCORD_USER_IDS));
    }

    protected static LongStream discordChannelIdsData() {
        return Streams.concat(discordCommonIdsData(), Arrays.stream(DISCORD_CHANNEL_IDS));
    }

    protected static LongStream discordBotIdsData() {
        return Streams.concat(discordCommonIdsData(), Arrays.stream(DISCORD_BOT_IDS));
    }

    protected static Stream<String> dictionaryIdsData() {
        return Arrays.stream(DICTIONARY_IDS);
    }

    protected static Stream<ReplaceType> dictionaryReplaceTypesData() {
        return Arrays.stream(ReplaceType.values());
    }

    protected static Stream<AutoDisconnectMode> autoDisconnectModesData() {
        return Arrays.stream(AutoDisconnectMode.values());
    }

    protected static Stream<String> voiceTypeNamesData() {
        return Arrays.stream(VOICE_TYPE_NAMES);
    }

    protected static Stream<String> voiceTypeNamesNullableData() {
        return Streams.concat(voiceTypeNamesData(), Stream.of((String) null));
    }

    protected static Stream<Pair<String, String>> customDictionaryData1() {
        return CUSTOM_DICTIONARY_ENTRIES_1.entrySet().stream()
                .map(it -> Pair.of(it.getKey(), it.getValue()));
    }

    protected static Stream<Pair<String, String>> customDictionaryData2() {
        return CUSTOM_DICTIONARY_ENTRIES_2.entrySet().stream()
                .map(it -> Pair.of(it.getKey(), it.getValue()));
    }

    protected static Stream<ServerDataRecord> serverDataRecordData() {
        return Arrays.stream(SERVER_DATA_RECORDS);
    }

    protected static Stream<ServerUserDataRecord> serverUserDataRecordData() {
        return Arrays.stream(SERVER_USER_DATA_RECORDS);
    }

    protected static Stream<DictionaryUseDataRecord> dictionaryUseDataRecordData() {
        return Arrays.stream(DICTIONARY_USE_DATA_RECORDS);
    }

    protected static Stream<Pair<TTSChannelPair, TTSChannelPair>> botStatePairsData() {
        return BOT_STATE_DATA_PAIR.stream();
    }

    protected static <T, U> Stream<Pair<T, U>> createTestDataStream(Stream<T> data1, Stream<U> data2) {
        // 2個のデータがすべてテストできるストリームを作成

        List<T> data1List = data1.toList();
        List<U> data2List = data2.toList();
        int maxNumOfData = Math.max(data1List.size(), data2List.size());

        return IntStream.range(0, maxNumOfData)
                .mapToObj(i ->
                        Pair.of(data1List.get(i % data1List.size()), data2List.get(i % data2List.size()))
                );
    }

    protected static Stream<Pair<Long, Long>> createTestDataStream(LongStream data1, LongStream data2) {
        return createTestDataStream(data1.boxed(), data2.boxed());
    }

    protected static <T, U, V> Stream<Triple<T, U, V>> createTestDataStream(Stream<T> data1, Stream<U> data2, Stream<V> data3) {
        // 3個のデータがすべてテストできるストリームを作成

        List<T> data1List = data1.toList();
        List<U> data2List = data2.toList();
        List<V> data3List = data3.toList();
        int maxNumOfData = Math.max(data1List.size(), Math.max(data2List.size(), data3List.size()));

        return IntStream.range(0, maxNumOfData)
                .mapToObj(i ->
                        Triple.of(data1List.get(i % data1List.size()), data2List.get(i % data2List.size()), data3List.get(i % data3List.size()))
                );
    }

    protected static <T, U, V, W> Stream<FNQuadruple<T, U, V, W>> createTestDataStream(Stream<T> data1, Stream<U> data2, Stream<V> data3, Stream<W> data4) {
        // 4個のデータがすべてテストできるストリームを作成

        List<T> data1List = data1.toList();
        List<U> data2List = data2.toList();
        List<V> data3List = data3.toList();
        List<W> data4List = data4.toList();
        int maxNumOfData = Math.max(Math.max(data1List.size(), data2List.size()), Math.max(data3List.size(), data4List.size()));

        return IntStream.range(0, maxNumOfData)
                .mapToObj(i ->
                        FNQuadruple.of(data1List.get(i % data1List.size()), data2List.get(i % data2List.size()), data3List.get(i % data3List.size()), data4List.get(i % data4List.size()))
                );
    }
}
