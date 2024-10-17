package dev.felnull.itts.core.savedata.dao;

/**
 * 辞書データのレコード
 *
 * @param target      置き換え対象
 * @param read        読み
 * @param replaceType 置き換え方法
 */
public record DictionaryRecord(String target, String read, int replaceType) {
}
