# felt Gradle Plugin

Feltは、Java向けんおライセンスヘッダー管理用の軽量なプラグインです。

## 説明

Feltは以下のことを行います:
- propertiesで設定されたライセンスによりLICENSE.mdとHEADERを生成
- HEADERの内容を、テストを含むJavaソースセット内のJavaファイルに適用

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

## ヘッダーの生成と適用

```shell
./gradlew generateLicense
./gradlew applyLicenseHeader
```

`LICENSE.md`にはライセンス本文、`HEADER`にはJavaソースに付けるコメントを保存します。
`generateLicenseHeader`でHEADERだけを生成することもできます。
既存のHEADERは上書きしないので、適用前に内容を編集できます。
`applyLicenseHeader`も、HEADERがなければ生成してから読み込みます。

`checkLicense`はLICENSE.mdと、ソースの先頭がHEADERと一致するかを検査します。
検査ではファイルを生成・変更しません。既存のソースヘッダーが異なる場合は、内容を確認して手動で更新してください。
HEADERにSPDX識別子を書く場合は、propertiesの`license`と揃えてください。

保存先を変える場合は、次のように指定します。

```kotlin
felt {
    headerFile = layout.projectDirectory.file("config/HEADER")
}
```

## 開発

リポジトリから以下の依存関係を追加します
`com.sumirelabs:felt-core:0.2.0`

## ライセンス

FeltはBlue Oak Model License 1.0.0の下でライセンスされます。
