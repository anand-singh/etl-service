package io.github.etl.service

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.github.etl.domain.ResponseHeader
import io.github.etl.exception.EtlServiceException
import io.github.etl.util.CommonUtility.buildResponseHeader
import io.github.etl.util.{CommonUtility, LoggerUtility, ResourceReader}
import org.http4s.EntityEncoder
import org.http4s.circe._

/**
  * AggregationService - Supports word count & frequency operations
  */
trait AggregationService[F[_]] {

  def wordCount(count: AggregationService.Count): F[AggregationService.AggregationResult]

  def wordFrequency(frequency: AggregationService.Frequency): F[AggregationService.AggregationResult]

}

object AggregationService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: AggregationService[F]): AggregationService[F] = ev

  final case class Count(requestId: String) extends AnyVal

  final case class Frequency(requestId: String) extends AnyVal

  /**
    * More Generally You will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class AggregationResult(header: ResponseHeader, result: Map[String, Int])

  object AggregationResult {
    implicit val wordCountEncoder: Encoder[AggregationResult] = (a: AggregationResult) => Json.obj(
      ("header", a.header.toJson),
      ("result", Json.fromString(a.result.mkString(",")))
    )

    implicit def wordCountEntityEncoder[F[_] : Applicative]: EntityEncoder[F, AggregationResult] =
      jsonEncoderOf[F, AggregationResult]
  }

  def impl[F[_] : Applicative]: AggregationService[F] = new AggregationService[F] {
    def wordCount(count: AggregationService.Count): F[AggregationService.AggregationResult] = {
      val wc = ResourceReader.words match {
        case Right(value) =>
          val header = CommonUtility.buildResponseHeader(count.requestId)
          AggregationResult(header, Map("count" -> value.size))
        case Left(th) => handleError(count.requestId, th)
      }
      wc.pure[F]
    }

    def wordFrequency(frequency: AggregationService.Frequency): F[AggregationService.AggregationResult] = {
      val wc = ResourceReader.words match {
        case Right(value) =>
          val header = CommonUtility.buildResponseHeader(frequency.requestId)
          AggregationResult(header, value.groupBy((word: String) => word).mapValues(_.length))
        case Left(th) => handleError(frequency.requestId, th)
      }
      wc.pure[F]
    }

  }

  private def handleError(requestId: String, th: EtlServiceException): AggregationResult = {
    error(th.getMessage, th)
    val header = buildResponseHeader(requestId, th)
    AggregationResult(header, Map.empty)
  }

}