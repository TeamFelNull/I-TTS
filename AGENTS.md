# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

I-TTS (Integration TTS) はDiscord用の読み上げBOT。VOICEVOX、COEIROINK、SHAREVOX、VoiceTextなどの音声合成APIに対応。

## ビルドコマンド

```bash
# ビルド (テストとCheckstyle含む)
./gradlew build

# テストのみ
./gradlew test

# 特定モジュールのテスト
./gradlew :core:test
./gradlew :selfhost:test

# 単一テストクラス実行
./gradlew :core:test --tests "dev.felnull.itts.core.savedata.repository.BotStateDataTest"

# Checkstyleのみ
./gradlew checkstyleMain

# Shadow JAR作成 (実行可能JAR)
./gradlew :selfhost:shadowJar
```

成果物は `selfhost/build/libs/itts-selfhost-{version}.jar` に生成される。

## アーキテクチャ

### モジュール構成

- **core**: BOTのコアロジック。JDAやLavaPlayerを使用したDiscord連携、音声合成、データ永続化
- **selfhost**: セルフホスト用エントリポイント。設定ファイル読み込みとランタイムコンテキスト提供

### マネージャーパターン

`ITTSRuntime`がシングルトンとして各マネージャーを保持:
- `ConfigManager`: 設定管理
- `VoiceManager`: 音声タイプ管理
- `TTSManager`: テキスト読み上げ処理
- `DictionaryManager`: 辞書管理
- `CacheManager`: 音声キャッシュ
- `SaveDataManager`: データ永続化

### 音声合成 (voice パッケージ)

抽象化レイヤー:
- `VoiceType`: 音声合成エンジン種別 (VOICEVOX, COEIROINK等)
- `VoiceCategory`: エンジン内のキャラクター
- `Voice`: 具体的な声スタイル

各エンジン実装は `voice.voicevox`, `voice.coeiroink`, `voice.voicetext` サブパッケージに配置。

### データ永続化 (savedata パッケージ)

- **dao**: データアクセスオブジェクト。SQLite/MySQL対応
- **repository**: ビジネスロジック向けリポジトリ層
- **legacy**: 旧バージョンデータ移行

スキーマ定義は [docs/schema-sqlite.md](docs/schema-sqlite.md) を参照。

### Discordコマンド (discord.command パッケージ)

`BaseCommand`を継承してスラッシュコマンドを実装。主要コマンド:
- `JoinCommand`, `LeaveCommand`: VC参加/退出
- `VoiceCommand`: 音声タイプ変更
- `DictCommand`: 辞書管理
- `ConfigCommand`: サーバー設定

## コードスタイル

Checkstyle (`config/checkstyle/checkstyle.xml`) で強制:
- `var` 禁止 (明示的な型宣言必須)
- 全publicメソッド/フィールドにJavadoc必須
- 行長170文字以内
- タブ文字禁止
