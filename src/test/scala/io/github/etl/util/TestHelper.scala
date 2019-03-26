package io.github.etl.util

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}

trait TestHelper {

  def dataSource: List[String] = ResourceReader.lines match {
    case Left(th) => List(th.getMessage)
    case Right(data) => data
  }

  /**
    * Return true if match succeeds; otherwise false
    */
  def check(actual: IO[Response[IO]],
            expectedStatus: Status,
            expectedBody: Option[String])(
             implicit ev: EntityDecoder[IO, String]
           ): Boolean = {
    val actualResp = actual.unsafeRunSync
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty)( // Verify Response's body is empty.
      expected => actualResp.as[String].unsafeRunSync.toLowerCase contains expected.toLowerCase
    )
    statusCheck && bodyCheck
  }

}
