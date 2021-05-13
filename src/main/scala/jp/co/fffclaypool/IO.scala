// quote
//   https://typelevel.org/cats-effect/docs/2.x/datatypes/io

/*
  IO
    副作用を純粋な値としてエンコードするためのデータ型で, 同期および非同期の計算を表現することができる

  はじめに
    IO[A]型の値は，評価されたときに，A型の値を返す前に効果を実行できる計算である
    IO型の値は純粋で不変的な値であるため，参照透過性が保たれ，関数型プログラミングで使用することができる
    IOは，副作用のある計算の単なる記述を表すデータ構造である．

    IOは，以下のような同期または非同期の計算を記述することができる
      1. 評価の結果，ちょうど1つの結果が得られる
      2. 結果は成功か失敗のいずれかであり，失敗の場合にはflatMapの連鎖が短絡する（IOはMonadErrorの代数を実装している）．
      3. キャンセル可能だが, この機能はユーザーがキャンセルロジックを提供することに依存していることに注意する.

    この抽象化によって記述された効果は, "世界の終わり"まで評価されない. つまり, 「安全でない」メソッドの1つが使用されたときに
    評価される. 効果の結果はメモ化されない. つまり, メモリのオーバーヘッドは最小限であり（リークもない）, また, 単一の効果を
    参照透過的に複数回実行することができる. 例えば,

      import cats.effect.IO

      val ioa = IO { println("hey!") }

      val program: IO[Unit] =
        for {
           _ <- ioa
           _ <- ioa
        } yield ()

      program.unsafeRunSync()
      //=> hey!
      //=> hey!
      ()

    上の例では, "hey!"が2回出力されるが, これはエフェクトがモナディックチェーンでシーケンスされるたびに再実行されるためである
    （ただし, version 2.xでは変数runがないので実行できないというエラーが出力される)
 */

/*
  参照元の透明性と遅延評価について
    標準ライブラリのFutureと何度も比較され, 評価モデルの観点から（Scalaの）状況を理解するためにこの分類を考えてみる

    ScalaのFutureと比較すると，IOデータ型は副作用を扱う場合でも参照透過性を維持し，遅延的に評価される．
    Futureと同様に, IOを使えば非同期プロセスの結果を推論することができるが, その純粋さと遅延性のために, IOは（「世界の終わり」
    に評価される）仕様として考えることができて, 評価モデルをより制御することができ, より予測可能になる

    Lazinessは参照の透明性と密接に関係している. 以下の例を考えてみる

      for {
        _ <- addToGauge(32)
        _ <- addToGauge(32)
      } yield ()

    参照元の透明性があれば、この例を次のように書き換えることができる

      val task = addToGauge(32)

      for {
        _ <- task
        _ <- task
      } yield ()
 */

/*
  エフェクトの記述
    IOは, 複数の種類の効果を効率的に記述できる強力な抽象である.

    純粋な値 - IO.pureとIO.unit
      純粋な値をIOに取り込み,「すでに評価されている」IOの値を得ることができる. IOのコンパニオンには次のような関数が
      定義されている.

        def pure[A](a: A): IO[A] = ???

      与えられたパラメータは名前ではなく値で渡されることに注意する.
      例えば，数値（純粋な値）をIOに取り込み，何も実行されないので安全な方法で副作用をラップした別のIOと合成することができる.

        IO.pure(25).flatMap(n => IO(println(s"Number is: $n")))

      IO.pureは, 与えられたパラメータが値で渡されている状態で評価されていて, 副作用を中断できないことは明らかなので,
      下記のようにしてはいけない

        IO.pure(println("THIS IS WRONG!"))

      この場合, printlnはIOでは中断されていない副作用を引き起こすことになり, このコードではおそらく意図していないことになる.
      IO.unitは単にIO.pure(())のエイリアスで, IO[Unit]の値が必要なときに使用できる再利用可能なリファレンスであるが, 他の副作用を
      引き起こす必要はない

        val unit: IO[Unit] = IO.pure(())

      ScalaのコードではIO[Unit]が非常によく使われており, Unitタイプ自体が副作用のあるルーチンの完了を知らせるためのものであることから
      同じ参照が返されるため, これはショートカットとしても最適化としても有用である.
 */

/*
  エラー処理
    Cats EffectにはMonadError[IO, Throwable]のインスタンスが存在するので, すべてのエラー処理はMonadErrorを介して行われる.
    つまり、エラータイプがThrowableである限り, MonadErrorやApplicativeErrorで利用可能なすべての操作をIO上で使用できる.
    raiseError, attempt, handleErrorWith, recoverWithなどの操作である. cats.syntax.all._などのsyntax importが
    スコープに入っていることを確認する.

    raiseError
      指定された例外をシーケンスするIOを構築する.

        import cats.effect.IO

        val boom: IO[Unit] = IO.raiseError(new Exception("boom"))
        boom.unsafeRunSync()
 */

/*
  "Unsafe"処理
     unsafeという接頭辞がついた演算はすべて不純な関数で副作用がある（例えば, HaskellにはunsafePerformIOがある）.
     しかし, 名前を聞いて怖がる必要はない. プログラムはmapやflatMapのような関数を使って他の関数を合成するモナディックな方法で書くべきで,
     理想的にはこれらのunsafeな操作をプログラムの最後に一度だけ呼び出すべきである.

    unsafeRunSync
      カプセル化された効果を不純な副作用として実行して結果を生成する. 計算のいずれかのコンポーネントが非同期の場合, 現在のスレッドは非同期計算の
      結果を待つためにブロックされる. JavaScriptでは, デッドロックの発生を避けるために代わりに例外がスローされる. デフォルトでは, このブロッキングは
      制限されない. スレッドブロックを一定時間に制限するには, 代わりに unsafeRunTimed を使用する. 効果内で発生した例外は、評価中に再度スローされる.

    unsafeRunAsync
      カプセル化されたエフェクトを不純な副作用として実行することで, その結果を与えられたコールバックに渡す.
      エフェクト内で発生した例外はEitherでコールバックに渡される. コールバックは最大で一度だけ起動される. スレッドをブロックせずに決してリターンしないIOを
      構築することは非常に可能であり, このメソッドでそのIOを評価しようとするとコールバックが決して呼び出されない状況になることに注意する
 */

/*
  map / flatMap での純粋な関数の使用
    mapやflatMapを使用する場合、副作用のある関数を渡すことは避けるべきである.

      IO.pure(123).map(n => println(s"NOT RECOMMENDED! $n"))

    これも避けるべきで、副作用は返されたIO値では中断されないからである.

      IO.pure(123).flatMap { n =>
        println(s"NOT RECOMMENDED! $n")
        IO.unit
      }

    正しいアプローチは次の通りである.

      IO.pure(123).flatMap { n =>
        // Properly suspending the side effect
        IO(println(s"RECOMMENDED! $n"))
      }

    IOの実際の動作に関する限り, IO.pure(x).map(f)のようなものはIO(f(x))と同等であり, IO.pure(x).flatMap(f)は
    IO.suspend(f(x))と同等であることに注意する. しかし, この動作を当てにしてはいけない. なぜならば、Sync 型クラスが
    要求する法則によって記述されておらず, これらの法則は動作の唯一の保証となるからである. 例えば, 上記の同等性は, エラー処理に
    関して将来的に破られるかもしれない. つまり, この動作は現在は安全のために存在していますが, 将来変更される可能性のある実装上の
    詳細であると考えるべきである.
 */

package jp.co.fffclaypool

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.std.Random

import scala.concurrent.duration._

object IOOperation extends IOApp {

  def sleepPrint(word: String, name: String, rand: Random[IO]) =
    for {
      delay <- rand.betweenInt(200, 700)
      _     <- IO.sleep(delay.millis)
      _     <- IO.println(s"$word, $name")
    } yield ()

  def run(args: List[String]) =
    for {
      rand <- Random.scalaUtilRandom[IO]
      name <- IO.pure("Daniel")

      // foreverM: 無限ループを発生させる
      english <- sleepPrint("Hello", name, rand).foreverM.start
      french  <- sleepPrint("Bonjour", name, rand).foreverM.start
      spanish <- sleepPrint("Hola", name, rand).foreverM.start

      _ <- IO.sleep(5.seconds)

      // 処理をキャンセルする
      _ <- english.cancel >> french.cancel >> spanish.cancel
    } yield ExitCode.Success
}
