package dev.felnull.itts.core.savedata.dao;

/**
 * サーバーと辞書で紐づけられたレコードを取得するためのキー
 *
 * @param serverKeyId     サーバーキーのID
 * @param dictionaryKeyId 辞書キーのID
 */
public record ServerDictionaryKey(int serverKeyId, int dictionaryKeyId) {
}
