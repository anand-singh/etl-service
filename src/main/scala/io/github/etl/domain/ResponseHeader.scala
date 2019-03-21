package io.github.etl.domain

import cats.Applicative
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

/**
  * TODO: Update with an appropriate comment
  *
  * @author Anand Singh (anand.singh@idemia.com)
  * @since 21-03-2019
  */
case class ResponseHeader
(
  requestId: String,
  responseId: String,
  statusCode: Long,
  statusMessage: String
) {

  def toJson: Json = this.asJson
}

object ResponseHeader {

  implicit val responseHeaderEncoder: Encoder[ResponseHeader] = (header: ResponseHeader) => {
    Json.obj(
      ("requestId", Json.fromString(header.requestId)),
      ("responseId", Json.fromString(header.responseId)),
      ("statusCode", Json.fromLong(header.statusCode)),
      ("statusMessage", Json.fromString(header.statusMessage))
    )
  }

  implicit def responseHeaderEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ResponseHeader] =
    jsonEncoderOf[F, ResponseHeader]

}


