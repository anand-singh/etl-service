package io.github.etl.service

import cats.Applicative
import cats.implicits._
import io.github.etl.constant.CommonConstant.Operations.{CAPS, REPLACE, WORD_COUNT, WORD_FREQUENCY}
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode.CODE_4001
import io.github.etl.domain.{EtlRequest, EtlSequence, Operation}
import io.github.etl.exception.EtlException
import io.github.etl.service.AggregationService.AggregationResult
import io.github.etl.service.SequenceService.SequenceRequest
import io.github.etl.service.TransformationService.TransformationResult
import io.github.etl.util.CommonUtility._
import io.github.etl.util.LoggerUtility

/**
  * Execute sequence process
  */
trait SequenceService[F[_]] {

  def applyTransformation(name: String, request: SequenceRequest, dataSource: List[String]): F[Option[TransformationResult]]

  def applyAggregation(name: String, request: SequenceRequest, dataSource: List[String]): F[Option[AggregationResult]]

}

object SequenceService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: SequenceService[F]): SequenceService[F] = ev

  final case class SequenceRequest(requestId: String, sequence: EtlSequence) extends EtlRequest

  def impl[F[_] : Applicative](TS: TransformationService[F], AS: AggregationService[F]): SequenceService[F] = new SequenceService[F] {

    def applyTransformation(name: String, request: SequenceRequest, dataSource: List[String]): F[Option[TransformationResult]] = {
      getOperation(name, request.sequence.etl) match {
        case None => Option.empty[TransformationResult].pure[F]
        case Some(operation) => executeTransformationOperation(request.requestId, operation, dataSource).map(Some(_))
      }
    }

    private[this] def executeTransformationOperation(reqId: String, operation: Operation, dataSource: List[String]): F[TransformationResult] = {
      Operations.getWithName(operation.opr) match {
        case CAPS => TS.caps(TransformationService.CapsRequest(reqId, dataSource))
        case REPLACE =>
          operation.body match {
            case None =>
              val header = buildResponseHeader(reqId, EtlException(CODE_4001, DATA_ERROR))
              TransformationResult(header, List.empty).pure[F]
            case Some(body) =>
              TS.replace(TransformationService.ReplaceRequest(reqId, body, dataSource))
          }
      }
    }

    def applyAggregation(name: String, request: SequenceRequest, dataSource: List[String]): F[Option[AggregationResult]] = {
      getOperation(name, request.sequence.etl) match {
        case None => Option.empty[AggregationResult].pure[F]
        case Some(operation) => executeAggregationOperation(request.requestId, operation, dataSource).map(Some(_))
      }
    }

    private[this] def executeAggregationOperation(reqId: String, operation: Operation, dataSource: List[String]): F[AggregationResult] = {
      Operations.getWithName(operation.opr) match {
        case WORD_COUNT => AS.wordCount(AggregationService.Count(reqId, dataSource))
        case WORD_FREQUENCY => AS.wordFrequency(AggregationService.Frequency(reqId, dataSource))
      }
    }

  }

  private[this] def getOperation(name: String, operations: List[Operation]): Option[Operation] = {
    operations.find { operation => operation.opr equalsIgnoreCase name }
  }

}
