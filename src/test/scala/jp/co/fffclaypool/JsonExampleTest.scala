package jp.co.fffclaypool

import io.circe.syntax._
import munit.FunSuite
import jp.co.fffclaypool.Attribute.Cat

class JsonExampleTest extends FunSuite {
  test("Row.toJson should get Platform object") {
    assertEquals(
      Row(
        Map(
          "sushi" -> "xydfa",
          "melon" -> "999",
        ).asJson.toString(),
        "drbws"
      ).toJson,
      Right(Platform(Cat("xydfa", "999"), "drbws"))
    )
  }

  test("Row.toJson should fail with Exception when failed to get sushi or melon") {
    val tcs = List(
      Row(
        Map("sush" -> "xydfa").asJson.toString(), "drbws"
      ),
      Row(
        Map("melon" -> "999").asJson.toString(), "drbws"
      ),
      Row(
        Map("hoge" -> "fuga").asJson.toString(), "drbws"
      )
    )

    tcs.foreach {m =>
      intercept[Exception](m.toJson.toTry.get)
    }
  }
}
