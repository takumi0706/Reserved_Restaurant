# レストラン予約管理システム

このプロジェクトは、Javaで実装されたレストラン予約管理システムです。このシステムは、テーブル予約の発行、キャンセル、および予約の再調整を行います。

## 機能概要

- 複数の異なる容量のテーブルを管理
- 特定のテーブルに対して指定された予約を発行
- 最適な利用可能なテーブルに対して未指定の予約を発行
- 予約のキャンセルおよび再調整
- 複数の予約時間枠の管理

## プロジェクト構成

- `test.java`: メインのエントリーポイント。標準入力からデータを読み取り、予約管理コマンドを処理します。
- `Restaurant.java`: レストランの予約管理ロジックを担当。
- `Table.java`: 各テーブルの予約情報を保持。
- `Reservation.java`: 各予約の詳細を保持。

## 使用方法

### 入力フォーマット

1. 最初の行: テーブルの数 `nTables`
2. 2行目: 各テーブルの容量を表す `nTables` 個の整数
3. 3行目: 営業時間、時間枠の数 `kSlots`、および `kSlots` の時間枠
4. 4行目以降: 予約管理コマンド。各コマンドはタイムスタンプ、コマンドタイプ、および関連するパラメータで構成されます。

#### 入力例

```
3
4 6 8
09:00-21:00 3 09:00-12:00 12:00-15:00 18:00-21:00
2024-05-25 08:00 issue-specified 1 20240526 1 2 1
2024-05-25 08:01 issue-unspecified 2 20240526 2 3
2024-05-25 08:02 cancel 1
2024-05-25 08:03 time 1
```

### 実行方法

1. このリポジトリをクローンまたはダウンロードします。
2. `test.java` ファイルを任意のJava IDEで開きます。
3. プロジェクトをビルドします。
4. コンソールから以下のコマンドを実行します:

```sh
javac test.java
java main.track.test < input.txt
```

### コマンド一覧

- `issue-specified`: 特定のテーブルに予約を発行します。
  - フォーマット: `issue-specified <ID> <予約日> <時間枠> <人数> <テーブルID>`
- `issue-unspecified`: 最適なテーブルに未指定の予約を発行します。
  - フォーマット: `issue-unspecified <ID> <予約日> <時間枠> <人数>`
- `cancel`: 予約をキャンセルします。
  - フォーマット: `cancel <ID>`
- `time`: 指定した時間枠の予約状況を表示します。
  - フォーマット: `time <時間枠>`

## クラスの詳細

### Restaurant

- **フィールド**
  - `int nTables`: テーブルの数
  - `Table[] tables`: 各テーブルの情報を保持する配列
  - `String runningHours`: 営業時間
  - `int kSlots`: 時間枠の数
  - `List<String> slots`: 各時間枠のリスト

- **メソッド**
  - `isCurrentSlot(int slot, String timestamp)`: 指定したスロットが現在のスロットかどうかを判定
  - `isPastSlot(int slot, String timestamp)`: 指定したスロットが過去のスロットかどうかを判定
  - `isOverCapacity(int tableId, int people)`: テーブルの容量を超えているかどうかを判定
  - `isTableOccupied(int tableId, int slot, int reservationDate)`: テーブルが既に予約されているかどうかを判定
  - `addReservation(int tableId, int slot, int reservationDate, int id, int people)`: 予約を追加
  - `findBestTable(int slot, int reservationDate, int people)`: 最適なテーブルを検索
  - `cancelReservation(int id, String timestamp)`: 予約をキャンセル
  - `isCancelable(Reservation reservation, String timestamp)`: 予約がキャンセル可能かどうかを判定
  - `adjustReservations()`: 予約を再調整
  - `clearReservations(int slot)`: 指定したスロットの予約をクリア
  - `printReservations(int slot, String timestamp)`: 指定したスロットの予約を表示

### Table

- **フィールド**
  - `int capacity`: テーブルの容量
  - `int tableNumber`: テーブル番号
  - `Map<Integer, Map<Integer, Reservation>> reservations`: 予約情報を保持するマップ

- **メソッド**
  - `isOccupied(int slot, int reservationDate)`: テーブルが予約されているかどうかを判定
  - `addReservation(int slot, int reservationDate, Reservation reservation)`: 予約を追加
  - `clearReservations(int slot)`: 指定したスロットの予約をクリア
  - `printReservations(int slot, String timestamp)`: 指定したスロットの予約を表示

### Reservation

- **フィールド**
  - `int id`: 予約ID
  - `int reservationDate`: 予約日
  - `int slot`: 時間枠
  - `int people`: 人数
  - `int tableId`: テーブルID
