package dev.felnull.itts.core.savedata.dao;

/**
 * サーバーとBOTで紐づけられたレコードを取得するためのキー
 *
 * @param serverKeyId サーバーキーのID
 * @param botKeyId    BOTキーのID
 */
public record ServerBotKey(int serverKeyId, int botKeyId) {
}
