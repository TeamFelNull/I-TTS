package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import dev.felnull.itts.core.util.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
     * デフォルトで有効な辞書
     */
    private final List<Dictionary> defaultEnableDictionaries = ImmutableList.of(
            globalDictionary,
            serverDictionary,
            abbreviationDictionary,
            romajiDictionary
    );

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
     * 指定されたサーバーで有効な辞書を優先度順に並べたリストを取得
     *
     * @param guildId サーバーID
     * @return 優先度昇順に並んだ辞書リスト
     */
    public List<Dictionary> getAllPriorityOrderEnableDictionaries(long guildId) {
        SaveDataManager saveDataManager = SaveDataManager.getInstance();
        List<DictionaryUseEntry> savedDictionaryUseEntries = saveDataManager.getRepository().getAllDictionaryUseData(guildId);

        // 保存されている有効な辞書使用データ
        Map<String, Integer> savedEnableDictAndPriority = savedDictionaryUseEntries.stream()
                .filter(it -> Boolean.TRUE.equals(it.enable()))
                .collect(Collectors.toMap(DictionaryUseEntry::dictionaryId, it -> {
                    int priority;
                    if (it.priority() != null) {
                        priority = it.priority();
                    } else {
                        Dictionary dict = getDictionary(it.dictionaryId(), guildId);
                        priority = dict != null ? dict.getDefaultPriority() : 0;
                    }
                    return priority;
                }));

        // 保存されているデフォルト定義で有効な辞書使用データ
        Map<String, Integer> savedDefaultEnableDictAndPriority = savedDictionaryUseEntries.stream()
                .filter(it -> {
                    if (it.enable() == null) {
                        return defaultEnableDictionaries.contains(getDictionary(it.dictionaryId(), guildId));
                    }
                    return false;
                })
                .collect(Collectors.toMap(DictionaryUseEntry::dictionaryId, it -> {
                    int priority;
                    if (it.priority() != null) {
                        priority = it.priority();
                    } else {
                        Dictionary dict = getDictionary(it.dictionaryId(), guildId);
                        priority = dict != null ? dict.getDefaultPriority() : 0;
                    }
                    return priority;
                }));

        // 保存されていないデフォルト定義で有効になっている辞書使用データ
        Map<String, Integer> defaultEnableDictAndPriority = defaultEnableDictionaries.stream()
                .filter(it -> savedDictionaryUseEntries.stream().noneMatch(data -> data.dictionaryId().equals(it.getId())))
                .collect(Collectors.toMap(Dictionary::getId, Dictionary::getDefaultPriority));

        Map<Dictionary, Integer> retDictAndPriority = new HashMap<>();

        savedEnableDictAndPriority.forEach((dictId, priority) -> {
            Dictionary dictionary = getDictionary(dictId, guildId);
            if (dictionary != null) {
                retDictAndPriority.put(dictionary, priority);
            }
        });

        savedDefaultEnableDictAndPriority.forEach((dictId, priority) -> {
            Dictionary dictionary = getDictionary(dictId, guildId);
            if (dictionary != null) {
                retDictAndPriority.put(dictionary, priority);
            }
        });

        defaultEnableDictAndPriority.forEach((dictId, priority) -> {
            Dictionary dictionary = getDictionary(dictId, guildId);
            if (dictionary != null) {
                retDictAndPriority.put(dictionary, priority);
            }
        });

        return retDictAndPriority.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 指定した辞書が有効かどうかを取得
     *
     * @param guildId サーバーID
     * @param dictId  辞書ID
     * @return 有効であればtrue
     */
    public boolean isEnable(long guildId, String dictId) {
        SaveDataManager saveDataManager = SaveDataManager.getInstance();
        Boolean savedEnable = saveDataManager.getRepository().getDictionaryUseData(guildId, dictId).isEnable();

        if (savedEnable != null) {
            return savedEnable;
        } else {
            Dictionary dict = getDictionary(dictId, guildId);
            return defaultEnableDictionaries.contains(dict);
        }
    }

    /**
     * 指定した辞書が有効どうかを設定する
     *
     * @param guildId サーバーID
     * @param dictId  辞書ID
     * @param enable  有効であればtrue
     */
    public void setEnable(long guildId, String dictId, boolean enable) {
        SaveDataManager saveDataManager = SaveDataManager.getInstance();
        saveDataManager.getRepository().getDictionaryUseData(guildId, dictId).setEnable(enable);
    }

    /**
     * テキストに辞書を適用する
     *
     * @param text    適用対象テキスト
     * @param guildId サーバーID
     * @return 適用済みテキスト
     */
    public String applyDict(String text, long guildId) {
        AtomicReference<String> retText = new AtomicReference<>(text);

        getAllPriorityOrderEnableDictionaries(guildId)
                .forEach(dict -> retText.set(dict.apply(retText.get(), guildId)));

        return retText.get();
    }


    /**
     * サーバー辞書をJsonに保存する
     *
     * @param jo      保存先Json
     * @param guildId サーバーID
     */
    public void serverDictSaveToJson(@NotNull JsonObject jo, long guildId) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        jo.addProperty("version", FILE_VERSION);

        JsonObject entry = new JsonObject();

        List<LegacyDictData> allDict = legacySaveDataLayer.getAllServerDictData(guildId);
        for (LegacyDictData dictData : allDict) {
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
    public List<LegacyDictData> serverDictLoadFromJson(@NotNull JsonObject jo, long guildId, boolean overwrite) {
        List<LegacyDictData> ret = new ArrayList<>();

        int version = JsonUtils.getInt(jo, "version", -1);

        if (version != FILE_VERSION) {
            throw new RuntimeException("Unsupported dictionary file version.");
        }

        if (jo.get("entry").isJsonObject()) {
            JsonObject entry = jo.getAsJsonObject("entry");
            LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();

            for (Map.Entry<String, JsonElement> en : entry.entrySet()) {
                String target = en.getKey();

                if (!en.getValue().isJsonPrimitive() || !en.getValue().getAsJsonPrimitive().isString()) {
                    continue;
                }

                String read = en.getValue().getAsString();

                LegacyDictData pre = legacySaveDataLayer.getServerDictData(guildId, target);

                if (!overwrite && pre != null) {
                    continue;
                }

                legacySaveDataLayer.addServerDictData(guildId, target, read);

                LegacyDictData ndata = Objects.requireNonNull(legacySaveDataLayer.getServerDictData(guildId, target));

                if (!ndata.equals(pre)) {
                    ret.add(ndata);
                }
            }
        }

        return ret;
    }
}
