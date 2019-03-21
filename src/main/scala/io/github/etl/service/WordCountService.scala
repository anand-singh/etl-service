package io.github.etl.service

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.github.etl.domain.ResponseHeader
import io.github.etl.util.{LoggerUtility, ResourceReader}
import org.http4s.EntityEncoder
import org.http4s.circe._
import io.github.etl.util.CommonUtility

trait WordCountService[F[_]] {
  def wordCount(count: WordCountService.Count): F[WordCountService.WordCount]
}

object WordCountService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: WordCountService[F]): WordCountService[F] = ev

  final case class Count(requestId: String) extends AnyVal

  /**
    * More Generally You will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class WordCount(header: ResponseHeader, count: Long)

  object WordCount {
    implicit val wordCountEncoder: Encoder[WordCount] = (a: WordCount) => Json.obj(
      ("header", a.header.toJson),
      ("result", Json.fromLong(a.count))
    )

    implicit def wordCountEntityEncoder[F[_] : Applicative]: EntityEncoder[F, WordCount] =
      jsonEncoderOf[F, WordCount]
  }

  def impl[F[_] : Applicative]: WordCountService[F] = (count: WordCountService.Count) => {
    val wc = ResourceReader.words match {
      case Right(value) =>
        val header = CommonUtility.buildResponseHeader(count.requestId)
        WordCount(header, value.size)
      case Left(th) =>
        error(th.getMessage, th)
        val header = CommonUtility.buildResponseHeader(count.requestId, th)
        WordCount(header, 0)
    }
    wc.pure[F]
  }

}