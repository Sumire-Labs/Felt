# felt Gradle Plugin

Feltは、Java向けんおライセンスヘッダー管理用の軽量なプラグインです。

## 説明

Feltは以下のことを行います:
- propertiesで設定されたライセンスによりLICENSE.mdの生成
- テストを含むJavaソースセット内のJavaファイルにSPDXヘッダーの追加

Feltを構成する2つのモジュールで構成されています。
- core: 依存関係を持たないjava8+ライブラリ
- gradle-plugin:　コアを使用したGradleプラグイン。Gradleバージョンは9.9.0でJava 17以降がサポートされています。

## サポートしているライセンス

MIT
GPL-2.0
GPL-3.0
LGPL-2.1
LGPL-3.0
Blue Oak 1.0.0

## 開発

リポジトリから以下の依存関係を追加します
`com.sumirelabs:felt-core:0.1.0-SNAPSHOT`

## ライセンス

FeltはBlue Oak Model License 1.0.0の下でライセンスされます。
