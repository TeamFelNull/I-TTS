package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.DictData;
import dev.felnull.itts.core.savedata.DictUseData;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.util.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * 辞書管理
 *
 * @author MORIMORI0317
 */
public class DictionaryManager implements ITTSRuntimeUse {

    /**
     * 辞書バージョン
     */
    private static final int FILE_VERSION = 0;

    /**
     * グローバル辞書
     */
    private final Dictionary globalDictionary = new GlobalDictionary();

    /**
     * サーバー辞書
     */
    private final Dictionary serverDictionary = new ServerDictionary();

    /**
     * 省略辞書
     */
    private final Dictionary abbreviationDictionary = new AbbreviationDictionary();

    /**
     * 単位辞書
     */
    private final Dictionary unitDictionary = new UnitDictionary();

    /**
     * ローマ字読み辞書
     */
    private final Dictionary romajiDictionary = new RomajiDictionary();

    /**
     * 全辞書のリスト
     */
    private final List<Dictionary> dictionaries = ImmutableList.of(globalDictionary, serverDictionary, abbreviationDictionary, unitDictionary, romajiDictionary);

    /**
     * 辞書を取得
     *
     * @param id      辞書ID
     * @param guildId サーバーID
     * @return 辞書
     */
    @Nullable
    public Dictionary getDictionary(@NotNull String id, long guildId) {
        return dictionaries.stream()
                .filter(r -> id.equals(r.getId()))
                .findAny()
                .orElse(null);
    }

    /**
     * 辞書が有効かどうか
     *
     * @param dictionary 辞書
     * @param guildId    サーバーID
     * @return 辞書が有効かどうか
     */
    public boolean isEnable(@NotNull Dictionary dictionary, long guildId) {
        SaveDataManager sdm = getSaveDataManager();
        DictUseData dud = sdm.getDictUseData(guildId, dictionary.getId());
        return dud.getPriority() >= 0;
    }

    /**
     * 全辞書を取得
     *
     * @param guildId サーバーID
     * @return 全辞書のリスト
     */
    @Unmodifiable
    @NotNull
    public List<Dictionary> getAllDictionaries(long guildId) {
        return dictionaries;
    }

    /**
     * 全辞書の使用データを取得
     *
     * @param guildId サーバーID
     * @return 使用データのリスト
     */
    @Unmodifiable
    @NotNull
    public List<DictUseData> getAllDictUseData(long guildId) {
        return dictionaries.stream()
                .map(it -> getSaveDataManager().getDictUseData(guildId, it.getId()))
                .toList();
    }

    /**
     * テキストに辞書を適用する
     *
     * @param text    適用対象テキスト
     * @param guildId サーバーID
     * @return 適用済みテキスト
     */
    public String applyDict(String text, long guildId) {
        Stream<DictUseData> allDict = getAllDictUseData(guildId).stream()
                .filter(it -> it.getPriority() >= 0)
                .sorted(Comparator.comparingInt(DictUseData::getPriority));
        AtomicReference<String> retText = new AtomicReference<>(text);

        allDict.forEach(ud -> {
            Dictionary dict = getDictionary(ud.getDictId(), guildId);

            if (dict != null) {
                retText.set(dict.apply(retText.get(), guildId));
            }
        });

        return retText.get();
    }

    @NotNull
    @Unmodifiable
    public List<Pair<String, Integer>> getDefault() {
        return ImmutableList.of(
                Pair.of(globalDictionary.getId(), globalDictionary.getDefaultPriority()),
                Pair.of(serverDictionary.getId(), serverDictionary.getDefaultPriority()),
                Pair.of(abbreviationDictionary.getId(), abbreviationDictionary.getDefaultPriority()),
                Pair.of(romajiDictionary.getId(), romajiDictionary.getDefaultPriority())
        );
    }

    /**
     * サーバー辞書をJsonに保存する
     *
     * @param jo      保存先Json
     * @param guildId サーバーID
     */
    public void serverDictSaveToJson(@NotNull JsonObject jo, long guildId) {
        jo.addProperty("version", FILE_VERSION);

        JsonObject entry = new JsonObject();

        List<DictData> allDict = getSaveDataManager().getAllServerDictData(guildId);
        for (DictData dictData : allDict) {
            entry.addProperty(dictData.getTarget(), dictData.getRead());
        }

        jo.add("entry", entry);
    }

    /**
     * Jsonからサーバー辞書を読み込む
     *
     * @param jo        読み込み対象Json
     * @param guildId   サーバーID
     * @param overwrite 上書きするかどうか
     * @return 辞書データのリスト
     */
    public List<DictData> serverDictLoadFromJson(@NotNull JsonObject jo, long guildId, boolean overwrite) {
        List<DictData> ret = new ArrayList<>();

        int version = JsonUtils.getInt(jo, "version", -1);

        if (version != FILE_VERSION) {
            throw new RuntimeException("Unsupported dictionary file version.");
        }

        if (jo.get("entry").isJsonObject()) {
            JsonObject entry = jo.getAsJsonObject("entry");
            SaveDataManager sdm = getSaveDataManager();

            for (Map.Entry<String, JsonElement> en : entry.entrySet()) {
                String target = en.getKey();

                if (!en.getValue().isJsonPrimitive() || !en.getValue().getAsJsonPrimitive().isString()) {
                    continue;
                }

                String read = en.getValue().getAsString();

                DictData pre = sdm.getServerDictData(guildId, target);

                if (!overwrite && pre != null) {
                    continue;
                }

                sdm.addServerDictData(guildId, target, read);

                DictData ndata = Objects.requireNonNull(sdm.getServerDictData(guildId, target));

                if (!ndata.equals(pre)) {
                    ret.add(ndata);
                }
            }
        }

        return ret;
    }
}
