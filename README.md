# IDiscordTTSVoice
The Ikisugi Discord TTS BOT  
DiscordのVCチャンネルで、指定されたテキストチャンネルに入力された文字を読み上げるBOT

### 対応済み読み上げAPI一覧
- [VOICEVOX](https://voicevox.hiroshiba.jp/)
- [COEIROINK](https://coeiroink.com/)
- [VoiceText](https://cloud.voicetext.jp/)
- [Google翻訳TTS](https://translate.google.co.jp/)
- [~~淫夢、クッキー☆~~](https://www.morimori0317.net/inc-sounds-search/)

VOICEVOXとCOEIROINKは自分でエンジンを起動しておく必要があり、VoiceTextにはAPIキーが必要です。

### 動作可能環境  
- Java17
- [LavaPlayer](https://github.com/walkyst/lavaplayer-fork)と[JDA](https://github.com/DV8FromTheWorld/JDA)が対応している環境(もしかしたらARMのMACなどは動かないかも)
- VOICEVOXとCOEIROINKのエンジンが対応している環境(VOICEVOXとCOEIROINKを利用する場合のみ)

Amd64のWindowsとLinuxで動作確認済み

### 利用方法
このBOTのJarファイルを引数なしで起動してください
```
java -jar IDiscordTTSVoice-1.23-all.jar
```
または
```
java17のディレクトリ\bin\java.exe -jar IDiscordTTSVoice-1.24.jar
```
コンフィグが存在しない場合は起動ディレクトリ内に自動生成されます  
コンフィグにBOTトークンなどを記述し、もう一度起動してください

### コンフィグ解説
#### コンフィグファイル
- "BotToken" BOTのトークン指定してください(複数指定可能)
- "VoiceVoxURL"、"CoeiroInkURL" それぞれのエンジンのURLを指定(複数指定可能)
- "VoiceTextAPIKey" VoiceTextのAPIキーを指定
- "CashTime" 読み上げ時の音声データのキャッシュ保持時間 （分)
- "IgnoreRegex" 読み上げを無視する文字列の正規表現
- "AdminRoles" 管理可能な役職ID
- "NeedAdminServers" AdminRolesで指定した役職を利用するサーバーのID
- "VoiceConfig" それぞれの読み上げの有効か無効か指定(利用しな読み上げAPIはfalseにしてください)
#### Discordでコマンドで変更可能なコンフィグ
- /config
  - need-join VCに参加時のみ読み上げるかどうか(True/False,デフォルトはFalse)
  - overwrite-aloud 読み上げを上書きするかどうか(True/False,デフォルトはFalse)
  - ~~inm-mode 淫夢モードを有効にするかどうか(True/False,デフォルトはFalse)~~
  - ~~cookie-mode クッキー☆モードを有効にするかどうか(True/False,デフォルトはFalse)~~
  - join-say-name 参加時に誰が参加したか読み上げるかどうか(True/False,デフォルトはFalse)
  - read-around-limit 最大読み上げ可能文字数(０以上の数値,デフォルトは200)(APIによっては最大文字数より少なくなります)
  - non-reading-prefix 先頭につけると読み上げなくなる文字(文字列,デフォルトは";")


