package jp.co.fffclaypool

import cats.effect.IO
import cats.implicits.catsSyntaxFlatMapOps
import cats.syntax.bifunctor._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.parser.decode

import jp.co.fffclaypool.Attribute.Cat

final case class Platform(attribute: Attribute, feed: String)

sealed trait Attribute
object Attribute {
  case class Cat(sushi: String, melon: String) extends Attribute
}

case class Row(attribute: String, feed: String) {
  import Util._
  def toJson: Either[Exception, Platform] ={
    /*
      Bifunctorは1つではなく2つの型のパラメータを取り, これらのパラメータの両方でファンクタとなる. また，関数bimapを
      定義しており，両方の引数を同時にマッピングすることができる. そのシグネチャーは以下の通りである.

        def bimap[C, D](f: A => C, g: B => D): F[C, D]

      Bifunctorのインスタンスで最も広く使われるのは, Either型である.

        dateTimeFromUser.bimap(
          error => DomainError(error.getMessage),
          dateTime => dateTime.toEpochSecond
        )
     */
    decode[Cat](attribute).bimap(
      e => new Exception("sushi or melon have not found"),
      Platform(_, feed)
    )
  }
  object Util {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    implicit val decodeDialogOneAttribute: Decoder[Cat] = deriveConfiguredDecoder[Cat]
  }
}

object JsonExample {
  def parse(row: List[Row]): IO[List[Platform]] = {
    /*
      flatMap
        overview:
          IOのモナディックバインドは, 2つのIOアクションを順次合成するために使用され, 最初のIOによって生成された値は,
          2番目のIOアクションを生成する関数の入力として渡される. この操作の特徴により, flatMapは2つのIOアクションの
          間にデータの依存関係を強制的に発生させ, シーケンスを確保する（例：あるアクションが別のアクションの前に実行される）.
          なぜなら, 非同期プロセスの性質上, 例外をキャッチして処理しなければ失敗は完全に黙殺され, IO参照が評価で終了することは
          ないからである
        usage:
          final def flatMap[B](f: A => IO[B]): IO[B]
            "[B]"は, 関数flatMapの型パラメータ
            "f: A => IO[B]"は, 関数flatMapの第一引数
              Aを引数としてIO[B]を戻り値とする関数を引数として取る
            最後の"IO[B]"は, 関数flatMapの戻り値 　　
     */

    /*
      >>=
        overview:
          flatMapのalias
        usage:
          def >>=[B](f: A => F[B])(implicit F: FlatMap[F]): F[B]
            "[B]"は, 関数>>=の型パラメータ
            "f: A => F[B]"は, 関数flatMapの第一引数
              Aを引数としてF[B]を戻り値とする関数を引数として取る
            最後の"F[B]"は, 関数>>=の戻り値
     */

    /*
      sequence
        overview:
          F[G[A]]からG[F[A]]に構造を反転させる
        usage:
          def sequence[G[_], B](implicit ev$1: A <:< G[B], ev$2: Applicative[G]): G[F[B]]
        example:
          List[Either[Exception, Platform]] -> Either[Exception, List[Platform]]
     */
    IO(row) flatMap (a => IO.fromEither(a.map(b => b.toJson).sequence))
    IO(row) >>= (a => IO.fromEither(a.map(b => b.toJson).sequence))
  }
}

/*
  参考:
    処理:
      def sample(f: (Int, Int) => Int, num: Int): Int = f(num,num)
      sample((x, y) => x + y, 12)
    1行目:
      "f: (Int, Int) => Int"は, sample関数の第1引数
        Int型の引数を2つ取り, 戻り値がInt
        という関数を引数にとっている
      "num: Int"は, sample関数の第2引数
      "): Int"は, sample関数の戻り値の型
      "="以降は, sample関数の実体
        第1引数で定義した関数に対して, 第2引数で与えられた数値を引数に渡す
    2行目:
      "(x, y) => x + y": sample関数に第1引数として渡す
      "12": sample関数に第2引数として渡す
 */
