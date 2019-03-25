package io.github.etl.service

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.CommonConstant.Operations._
import io.github.etl.domain.{ResponseHeader, SequenceRequestData}
import io.github.etl.exception.EtlServiceException
import io.github.etl.util.CommonUtility._
import io.github.etl.util.{LoggerUtility, ResourceReader}
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
      info(s"Received sequence request for validation: $request")
      val result = if (validateSequenceOperations(request.sequence.etl)) {
        SequenceResult(buildResponseHeader(request.requestId), Json.obj())
      } else {
        handleError(request.requestId, EtlServiceException(message = OPERATION_ERROR))
      }
      result.pure[F]
    }

    def executeSequence(request: SequenceRequest): F[SequenceResult] = {
      info(s"Received sequence request: $request")
      TS.caps(TransformationService.CapsRequest(request.requestId))
      val result = ResourceReader.lines match {
        case Right(value) =>
          //TODO: Need to fix the operation execution
          val result = request.sequence.etl.map { operation =>
            Operations.getWithName(operation.opr) match {
              case CAPS => TS.caps(TransformationService.CapsRequest(request.requestId)).map { data => Json.obj(("caps", Json.fromValues(data.result.map(Json.fromString)))) }
              case REPLACE => TS.replace(TransformationService.ReplaceRequest(request.requestId, operation.body.get.from, operation.body.get.to)).map { data => Json.obj(("replace", Json.fromValues(data.result.map(Json.fromString)))) }
              case WORD_COUNT => AS.wordCount(AggregationService.Count(request.requestId)).map { data => Json.obj(("wordCount", Json.fromValues(data.result.map { case (key, value) => Json.obj((key, Json.fromInt(value))) }))) }
              case WORD_FREQUENCY => AS.wordFrequency(AggregationService.Frequency(request.requestId)).map { data => Json.obj(("wordCount", Json.fromValues(data.result.map { case (key, value) => Json.obj((key, Json.fromInt(value))) }))) }
            }
          }
          val header = buildResponseHeader(request.requestId)
          SequenceResult(header, Json.obj())
        case Left(th) => handleError(request.requestId, th)
      }
      result.pure[F]
    }

  }

  private def handleError(requestId: String, th: EtlServiceException): SequenceResult = {
    error(s"Error occurred: ${th.getMessage}", th)
    val header = buildResponseHeader(requestId, th)
    SequenceResult(header, Json.obj())
  }
}
