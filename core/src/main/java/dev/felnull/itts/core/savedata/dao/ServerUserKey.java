package dev.felnull.itts.core.savedata.dao;

/**
 * サーバーとユーザーで紐づけられたレコードを取得するためのキー
 *
 * @param serverKeyId サーバーキーのID
 * @param userKeyId   ユーザーキーのID
 */
public record ServerUserKey(int serverKeyId, int userKeyId) {
}
