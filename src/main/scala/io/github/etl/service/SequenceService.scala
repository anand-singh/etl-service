package io.github.etl.service

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.github.etl.domain.{ResponseHeader, SequenceRequestData}
import io.github.etl.exception.EtlServiceException
import io.github.etl.util.CommonUtility.buildResponseHeader
import io.github.etl.util.{CommonUtility, LoggerUtility, ResourceReader}
import org.http4s.EntityEncoder
import org.http4s.circe._

/**
  * Execute sequence process
  */
trait SequenceService[F[_]] {

  def validateSequence(request: SequenceService.SequenceRequest): F[SequenceService.SequenceResult]

  def executeSequence(request: SequenceService.SequenceRequest): F[SequenceService.SequenceResult]

}

object SequenceService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: SequenceService[F]): SequenceService[F] = ev

  final case class SequenceRequest(requestId: String, sequence: SequenceRequestData)

  final case class SequenceResult(header: ResponseHeader, result: Json)

  object SequenceResult {
    implicit val sequenceResultEncoder: Encoder[SequenceResult] = (a: SequenceResult) => Json.obj(
      ("header", a.header.toJson),
      ("result", a.result)
    )

    implicit def transResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, SequenceResult] =
      jsonEncoderOf[F, SequenceResult]
  }

  def impl[F[_] : Applicative](TS: TransformationService[F], AS: AggregationService[F]): SequenceService[F] = new SequenceService[F] {

    override def validateSequence(request: SequenceRequest): F[SequenceResult] = {
      val result = if (CommonUtility.validateSequence(request.sequence)) {
        val header = buildResponseHeader(request.requestId)
        SequenceResult(header, Json.obj())
      } else {
        handleError(request.requestId, EtlServiceException(message = "Can not sequence non logical operations."))
      }
      result.pure[F]
    }

    def executeSequence(request: SequenceRequest): F[SequenceResult] = {
      TS.caps(TransformationService.CapsRequest(request.requestId))
      val result = ResourceReader.lines match {
        case Right(value) =>
          val header = buildResponseHeader(request.requestId)
          SequenceResult(header, Json.obj())
        case Left(th) => handleError(request.requestId, th)
      }
      result.pure[F]
    }

  }

  private def handleError(requestId: String, th: EtlServiceException): SequenceResult = {
    error(th.getMessage, th)
    val header = buildResponseHeader(requestId, th)
    SequenceResult(header, Json.obj())
  }
}
