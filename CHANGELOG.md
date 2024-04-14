# Changelog
このBOTの更新を追跡するための更新ログ。   
変更をコミットする場合は`Unreleased`に更新内容を追記してください。  
[Keep a Changelog](https://keepachangelog.com/en/1.0.0/)に従って記述をお願い致します。

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [2.0.0-alpha.10] - 2024-04-14

### Fixed
- 不要なIntentsを削除

## [2.0.0-alpha.9] - 2024-02-11

### Fixed
- 起動時に読み上げチャンネルへの接続権限不足、チャンネルが消滅してた場合のログを修正、正しく保存データを更新するように変更

## [2.0.0-alpha.8] - 2023-09-21

### Fixed
- サーバーニックネームが存在しない場合に、ユーザーニックネームではなくユーザーIDを読み上げていた問題を修正。

## [2.0.0-alpha.7] - 2023-09-03

### Changed
- 辞書の適用順番を、省略辞書が一番最初に適用されるように変更

## [2.0.0-alpha.6] - 2023-08-29

### Fixed
- スレッドが大量生成されOutOfMemoryになる問題を修正

## [2.0.0-alpha.5] - 2023-07-27

### Fixed
- 返信またはピン止め時に読まれるユーザ名がI-TTSのニックネームを参照していなかった問題を修正

## [2.0.0-alpha.4] - 2023-07-26

### Fixed
- VCに参加していないユーザのメンションを読み上げる際にニックネームではなくIDを読み上げる問題を修正

## [2.0.0-alpha.3] - 2023-06-09

### Fixed
- タイマーの処理中にエラーが発生した場合、タイマーが停止する問題を修正

## [2.0.0-alpha.2] - 2023-06-05

### Fixed
- ピン留めされた時に読み上げられるメッセージの不具合を修正

## [2.0.0-alpha.1] - 2023-06-03

### Added
- 初期リリース

[Unreleased]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.10...HEAD
[2.0.0-alpha.9]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.8...v2.0.0-alpha.9
[2.0.0-alpha.1]: https://github.com/TeamFelnull/I-TTS/commits/v2.0.0-alpha.1
[2.0.0-alpha.4]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.3...v2.0.0-alpha.4
[2.0.0-alpha.3]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.2...v2.0.0-alpha.3
[2.0.0-alpha.2]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.1...v2.0.0-alpha.2
[2.0.0-alpha.5]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.4...v2.0.0-alpha.5
[2.0.0-alpha.6]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.5...v2.0.0-alpha.6
[2.0.0-alpha.7]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.6...v2.0.0-alpha.7
[2.0.0-alpha.8]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.7...v2.0.0-alpha.8
[2.0.0-alpha.10]: https://github.com/TeamFelnull/I-TTS/compare/v2.0.0-alpha.9...v2.0.0-alpha.10
