package dev.felnull.itts.core.savedata.dao;

/**
 * 辞書データのレコード
 *
 * @param target           置き換え対象
 * @param read             読み
 * @param replaceTypeKeyId 置き換えタイプのキーID
 */
public record DictionaryRecord(String target, String read, int replaceTypeKeyId) {
}
