---
name: update-changelog
description: 'Append entries to the Unreleased section of CHANGELOG.md following Keep a Changelog and the gradle-changelog-plugin conventions'
---

# CHANGELOG.md の Unreleased への追記

このスキルは、リポジトリルートにある `CHANGELOG.md` の `## [Unreleased]` セクションに変更内容を追記するためのものです。
このリポジトリは [JetBrains/gradle-changelog-plugin](https://github.com/JetBrains/gradle-changelog-plugin)（`org.jetbrains.changelog`）を `build.gradle.kts` で使用しており、書式は [Keep a Changelog 1.0.0](https://keepachangelog.com/en/1.0.0/) に準拠しています。

**スコープ**: このスキルは「Unreleased への追記」のみを扱います。
バージョン確定（`patchChangelog`）、日付の付与、`[x.y.z]: ...compare/...` のリンク追加、新しいバージョン見出しの作成は **行いません**。

## 前提

- ファイル: リポジトリルートの `CHANGELOG.md`
- 言語: 日本語（既存エントリに合わせる）
- 既存の `## [Unreleased]` セクションには、以下 6 つのカテゴリ見出しがすべて配置済み:
  - `### Added` — 新機能
  - `### Changed` — 既存機能の変更
  - `### Deprecated` — 近く削除される機能
  - `### Removed` — 削除された機能
  - `### Fixed` — バグ修正
  - `### Security` — 脆弱性に関する修正

## 手順

1. `CHANGELOG.md` を開き、`## [Unreleased]` 行を見つける。
2. 変更内容に合致するカテゴリ（後述「カテゴリの選び方」）を 1 つ選ぶ。
3. そのカテゴリ見出し直下に、`- ` で始まる箇条書き 1 行を **追加** する。既存の項目があれば末尾に追記する。
4. **空のカテゴリ見出しを削除しない**。空のままで残す（gradle-changelog-plugin が空セクションを許容している既存運用に従う）。
5. **`## [Unreleased]` 以外のリリース見出し（`## [2.1.2] - 2026-05-04` など）と、ファイル末尾の `[x.y.z]: ...` 参照リンクは編集しない**。

## カテゴリの選び方

| カテゴリ      | 用途                                                                  |
| ------------- | --------------------------------------------------------------------- |
| `Added`       | 新しいコマンド・機能・対応エンジン・設定項目などの追加                |
| `Changed`     | 既存機能の挙動・UI・ライブラリ更新など、互換性を破らない変更          |
| `Deprecated`  | 将来削除予定の機能をユーザに告知する場合                              |
| `Removed`     | コマンド・機能・設定項目の削除                                        |
| `Fixed`       | バグ・不具合の修正                                                    |
| `Security`    | セキュリティ上の脆弱性に対する修正                                    |

判断に迷う典型例:
- ライブラリのバージョンアップで挙動が改善する → `Changed`（バグ修正目的なら `Fixed`）
- 内部リファクタリングのみでユーザに影響がない → **追記しない**
- セキュリティ修正でかつ機能変更も伴う → `Security` を優先

## 記述スタイル

- **日本語**で書く。
- **ユーザ視点**で書く（クラス名・メソッド名・内部実装詳細は書かない）。
- **体言止め** または「〜を修正」「〜に対応」「〜を追加」のような短い末尾で統一。文末の句点（`。`）は既存エントリに合わせて省略する。
- 1 項目につき 1 行。長くなる場合は 2 項目に分ける。
- 該当する Issue / PR 番号を残したい場合は末尾に `(#123)` のように付与してよい（既存運用では必須ではない）。

## やってはいけないこと

- 既存のリリース済みセクション（`## [2.1.2]` など）の内容を変更する。
- 新しいバージョン見出し（`## [2.1.3] - YYYY-MM-DD` など）を追加する。
- ファイル末尾の `[Unreleased]: .../compare/v2.1.2...HEAD` などの参照リンクを編集・追加する。
- `Unreleased` セクション冒頭の説明文（`introduction`）を編集する。これは `build.gradle.kts` の `changelog { introduction.set(...) }` で管理されている。
- 空のカテゴリ見出しを削除する。

## 良い例

```markdown
## [Unreleased]

### Added
- VOICEVOXの新キャラクターに対応

### Changed

### Deprecated

### Removed

### Fixed
- 長文メッセージで読み上げが途中で止まる問題を修正

### Security
```

## 悪い例

```markdown
## [Unreleased]

### Added
- `TTSManager#speak` メソッドに `priority` パラメータを追加し、内部キューの実装を `PriorityQueue` に変更した。  ← 内部実装の説明、長すぎ、句点あり

## [2.1.3] - 2026-05-16  ← リリース確定はこのスキルの対象外
### Fixed
- 何かを修正
```

## 検証

追記後は以下を目視で確認すれば十分です（コマンド実行は不要）:

- `## [Unreleased]` 配下の正しいカテゴリに 1 行追加されている。
- 6 カテゴリの見出しがすべて残っている。
- リリース済みセクション・参照リンクに変更がない。
