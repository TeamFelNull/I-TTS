# I-TTS (Integration ~~Ikisugi~~ TTS)

![A](./.github/readme/logo.png)
***

いろいろな機能が統合されたDiscordの読み上げBOT

## 機能

- 指定されたテキストチャンネルの会話を読み上げ
- VCに参加、移動してきたユーザの名前を読み上げ
- サーバーごと、ユーザーごとに読み上げる声を変更
- サーバーごと、グローバルで指定可能な読み上げ辞書機能

## 対応済み読み上げAPI

- [VOICEVOX](https://voicevox.hiroshiba.jp/)
- [COEIROINK](https://coeiroink.com/)
- [SHAREVOX](https://www.sharevox.app/)
- [VoiceText](https://cloud.voicetext.jp/)

VOICEVOXとCOEIROINK、SHAREVOXは自分でエンジンを起動しておく必要があり、VoiceTextにはAPIキーが必要です。

## 動作環境

- Java17
- [LavaPlayer](https://github.com/walkyst/lavaplayer-fork)と[JDA](https://github.com/DV8FromTheWorld/JDA)が対応しているプラットフォーム

Windows(amd64)とLinux((amd64,arm64)で動作確認済み。  
VOICEVOXとCOEIROINK、SHAREVOXを利用する場合はそれぞれが対応してる環境が必要です。

## 起動方法

このBOTのJarファイルを引数なしで起動してください

```
java -jar itts-selfhost-2.0.0.jar
```

または

```
java17のディレクトリ\bin\java.exe -jar itts-selfhost-2.0.0.jar
```

初回起動時はコンフィグが起動ディレクトリ内に生成され停止します。  
コンフィグにBOTトークンなどを記述し、もう一度起動してください。

## コンフィグ

### BOT

起動時に生成されるconfig.json5で指定可能です。  
起動後は変更不可です。

* "bot_token" BOTのトークン
* "theme_color" BOTのテーマカラー(埋め込みテキストなどに利用される色)
* "cache_time" 音声データのキャッシュの保持期間(ミリ秒)
* "voice_text" VoiceTextに関するコンフィグ
    * "enable" VoiceTextを有効にするかどうか
    * "api_key" VoiceTextのAPIキー
* "voicevox"、"coeirolnk"、"sharevox" それぞれVOICEVOX系に関するコンフィグ
    * "enable" それぞれVOICEVOX系の読み上げを有効にするかどうか
    * "api_url" それぞれVOICEVOX系のエンジンのURL(複数指定可能)
    * "check_time" それぞれVOICEVOX系のエンジンが生きているか確認する間隔(ミリ秒)

### サーバー

サーバー別に/configコマンドで変更可能です。

* "notify-move" VCの入退室時にユーザー名を読み上げるかどうか
* "read-limit" 読み上げ文字数上限
* "name-read-limit" 名前の読み上げ文字数上限
* "need-join" VCに参加中のユーザーのみ読み上げるかどうか
* "read-overwrite" 読み上げの上書きするかどうか
* "read-ignore" 読み上げない文字(正規表現)
* "default-voice" 初期の読み上げタイプ

## コマンド

一部のコマンドは初期状態でサーバー管理者のみ利用可能です。  
サーバー管理者以外が利用したい場合はサーバーでスラッシュコマンドの権限を指定する必要があります。

* /join 読み上げBOTをVCに呼び出す、チャンネル指定可能(未指定の場合は自分が接続してるVC)
* /leave 読み上げBOTをVCから切断する
* /reconnect 読み上げBOTを再接続させます(読み上げるチャンネルを変更、調子が悪い時に利用してください)
* /voice 読み上げ音声タイプ関連
    * change 自分の読み上げ音声タイプを変更する
    * check 自分の読み上げ音声タイプを確認する
    * show 読み上げ音声タイプ一覧を表示します
* /vnick 自分の読み上げユーザ名を変更する
* /info 情報表示関係
    * about BOT情報を表示する
    * oss OSS情報を表示する
    * work OSS情報を表示する
* /config コンフィグ関係(詳細は[コンフィグ](#コンフィグ)を確認してください)
* /deny 読み上げ拒否関連
    * add 読み上げ拒否リストにユーザーを追加する
    * remove 読み上げ拒否リストからユーザーを削除する
    * show 読み上げ拒否リストを表示します
* /admin 他人の設定を変更
    * vnick 他人の読み上げユーザ名を変更する
    * voice 読み上げ音声タイプ関連
        * change 他人の読み上げ音声タイプを変更する
        * check 他人の読み上げ音声タイプを確認する
* /dict 読み上げ辞書関連
    * toggle 辞書ごとの有効無効を切り替える
    * toggle-show 辞書ごとの有効無効を表示する
    * show サーバー読み上げ辞書の内容を表示する
    * add サーバー読み上げ辞書に単語を登録する
    * remove サーバー読み上げ辞書から単語を削除する
    * download サーバー読み上げ辞書をダウンロードする
    * upload サーバー読み上げ辞書をアップロードする

## 旧バージョン

v2.xはプレビュー版です、安定しているバージョンを必要とする場合は[v1.x](https://github.com/TeamFelnull/I-TTS/tree/1.x)を利用してください。