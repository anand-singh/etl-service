package io.github.etl.service

import cats.Applicative
import cats.effect.Async
import io.circe.Encoder
import io.github.etl.constant.CommonConstant._
import io.github.etl.domain.{EtlRequest, EtlResult, ResponseHeader}
import io.github.etl.util.CommonUtility._
import io.github.etl.util.LoggerUtility
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

  final case class Count(requestId: String, dataSource: List[String]) extends EtlRequest

  final case class Frequency(requestId: String, dataSource: List[String]) extends EtlRequest

  final case class AggregationResult(header: ResponseHeader, result: Map[String, Int]) extends EtlResult

  object AggregationResult {
    implicit val wordCountEncoder: Encoder[AggregationResult] = (aggrResult: AggregationResult) =>
      etlResultToJson(RESULT_TEXT, aggrResult)

    implicit def wordCountEntityEncoder[F[_] : Applicative]: EntityEncoder[F, AggregationResult] =
      jsonEncoderOf[F, AggregationResult]
  }

  def impl[F[_] : Async]: AggregationService[F] = new AggregationService[F] {
    def wordCount(count: AggregationService.Count): F[AggregationService.AggregationResult] = Async[F].delay {
      info(s"Received word count request: $count")
      val countResult = (value: List[String]) => Map(COUNT_TEXT -> value.size)
      processAggregationResult(count.requestId, count.dataSource, countResult)
    }

    def wordFrequency(frequency: AggregationService.Frequency): F[AggregationService.AggregationResult] = Async[F].delay {
      info(s"Received word frequency request: $frequency")
      val frequencyResult = (value: List[String]) => value.groupBy((word: String) => word).view.mapValues(_.length).toMap
      processAggregationResult(frequency.requestId, frequency.dataSource, frequencyResult)
    }

  }

  private[this] def processAggregationResult(reqId: String,
                                             dataSource: List[String],
                                             f: List[String] => Map[String, Int]
                                            ): AggregationResult = {
    val words = dataSource.flatMap(_.split(" "))
    val header = buildResponseHeader(reqId)
    AggregationResult(header, f(words))
  }

}
