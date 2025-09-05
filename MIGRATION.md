# JSON to SQL Migration

この機能は、古いJSONファイルからSQLデータベースへのデータ移行を自動的に実行します。

## 対象ファイル

以下のJSONファイルが起動ディレクトリに存在する場合、自動的にSQLデータベースに移行されます：

- `server_data.json` - サーバー設定データ
- `user_data.json` - ユーザー設定データ  
- `dict_data.json` - サーバー別辞書データ
- `global_dict_data.json` - グローバル辞書データ

## JSONファイル形式例

### server_data.json
```json
{
  "123456789": {
    "defaultVoiceType": "voice_type_name",
    "ignoreRegex": "regex_pattern",
    "needJoin": true,
    "overwriteAloud": false,
    "notifyMove": true,
    "readLimit": 100,
    "nameReadLimit": 15
  }
}
```

### user_data.json
```json
{
  "123456789": {
    "987654321": {
      "voiceType": "user_voice_type",
      "deny": false,
      "nickName": "ユーザー名"
    }
  }
}
```

### dict_data.json
```json
{
  "123456789": {
    "読み替え対象": "よみかえたいしょう",
    "test": "てすと"
  }
}
```

### global_dict_data.json
```json
{
  "グローバル": "ぐろーばる",
  "共通": "きょうつう"
}
```

## 移行プロセス

1. アプリケーション起動時にJSONファイルの存在を確認
2. ファイルが存在し、`.migration_completed` マーカーファイルが存在しない場合、移行を実行
3. JSONデータをSQLデータベースに変換・保存
4. 移行完了後、元のJSONファイルを `json_backup/` ディレクトリに移動
5. `.migration_completed` マーカーファイルを作成して移行完了を記録

## 注意事項

- 移行は1度だけ実行されます
- 元のJSONファイルはバックアップディレクトリに保存されます
- 移行中にエラーが発生した場合、ログに記録されます
- 手動で移行を再実行したい場合は、`.migration_completed` ファイルを削除してください