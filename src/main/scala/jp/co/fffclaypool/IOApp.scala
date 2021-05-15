/*
  IOApp
    IOAppは安全なアプリケーションタイプで, 純粋な関数型プログラムのエントリーポイントとして, cats.effect.IOを
    実行するmain関数を記述する.

  Status Quo
    現在，純粋なFPプログラムを実行するJavaアプリケーションのエントリポイントを指定するためには，次のようにしなければなりません（副作用は中断され，IOで記述されます）。
 */
/*
import cats.effect._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Main {
  // Needed for `IO.sleep`
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def program(args: List[String]): IO[Unit] =
    IO.sleep(1.second) *> IO(println(s"Hello world!. Args $args"))

  def main(args: Array[String]): Unit =
    program(args.toList).unsafeRunSync()
}
 */
