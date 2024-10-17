package dev.felnull.itts.core.savedata;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.felnull.itts.core.dict.ReplaceType;
import dev.felnull.itts.core.discord.AutoDisconnectMode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
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

    private static final String[] DICTIONARY_NAMES = {
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

    protected static Stream<String> dictionaryNamesData() {
        return Arrays.stream(DICTIONARY_NAMES);
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
}
