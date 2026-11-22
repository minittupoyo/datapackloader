# DatapackLoader

Paper 26.3 向けのプラグインです。ワールドの `datapacks` ディレクトリではなく、**プラグインのフォルダ**でデータパックを管理して読み込みます。

## 特長

- `plugins/DatapackLoader/datapacks/` 内のフォルダ形式・`.zip` 形式のデータパックを読み込む
- ワールドを作り直したり入れ替えたりしても、データパックはそのまま使える
- サーバー起動時に自動で有効化
- `/datapackloader reload` で、サーバーを再起動せずに再読み込み

## 必要環境

- Paper 26.3
- Java 25

## インストール

1. [Releases](../../releases) または自分でビルドした JAR を `plugins/` に入れる
2. サーバーを一度起動する（`plugins/DatapackLoader/datapacks/` が作られる）
3. 読み込みたいデータパックをそのフォルダに入れる
4. サーバーを再起動、または `/datapackloader reload` を実行する

## コマンド

| コマンド | 説明 | 権限 |
| --- | --- | --- |
| `/datapackloader reload`（`/dpl reload`） | フォルダを再スキャンし、新しいパックを有効化してデータを再読み込み | `datapackloader.reload`（既定は OP） |

## 仕様

- パック ID はファイル名（`.zip` を除く）を小文字化し、`[a-z0-9_.-]` 以外を `_` に置換したものです。
- `pack.mcmeta` が無いなど読み込めないパックは、警告ログを出してスキップします。
- 実装は Paper の `LifecycleEvents.DATAPACK_DISCOVERY` を使っています。

## ビルド

JDK 25 が必要です（Gradle の toolchain が自動取得することもできます）。

```sh
gradle build
```

成果物は `build/libs/datapackloader-1.0.0.jar` です。

## ライセンス

[MIT](LICENSE)
