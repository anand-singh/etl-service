package io.github.etl.service

import java.util.regex.PatternSyntaxException

import cats.Applicative
import cats.implicits._
import io.circe.Encoder
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{EtlRequest, EtlResult, OperationBody, ResponseHeader}
import io.github.etl.exception.EtlException
import io.github.etl.util.CommonUtility._
import io.github.etl.util.LoggerUtility
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

/**
  * TransformationService - Supports caps & replace operations
  */
trait TransformationService[F[_]] {

  def caps(request: TransformationService.CapsRequest): F[TransformationService.TransformationResult]

  def replace(request: TransformationService.ReplaceRequest): F[TransformationService.TransformationResult]

}

object TransformationService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: TransformationService[F]): TransformationService[F] = ev

  final case class CapsRequest(requestId: String, dataSource: List[String]) extends EtlRequest

  final case class ReplaceRequest(requestId: String, body: OperationBody, dataSource: List[String]) extends EtlRequest

  final case class TransformationResult(header: ResponseHeader, result: List[String]) extends EtlResult

  object TransformationResult {
    implicit val transResultEncoder: Encoder[TransformationResult] = (tr: TransformationResult) =>
      etlResultToJson("result", tr)

    implicit def transResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, TransformationResult] =
      jsonEncoderOf[F, TransformationResult]
  }

  def impl[F[_] : Applicative]: TransformationService[F] = new TransformationService[F] {
    def caps(request: TransformationService.CapsRequest): F[TransformationService.TransformationResult] = {
      info(s"Received caps request: $request")
      val header = buildResponseHeader(request.requestId)
      TransformationResult(header, request.dataSource.map(_.toUpperCase)).pure[F]
    }

    def replace(request: TransformationService.ReplaceRequest): F[TransformationService.TransformationResult] = {
      info(s"Received replace request: $request")
      val result = doReplace(request) match {
        case Right(value) =>
          val header = buildResponseHeader(request.requestId)
          TransformationResult(header, value)
        case Left(th: PatternSyntaxException) =>
          val exp = EtlException(CODE_4002, SYNTAX_ERROR, th)
          handleError(request.requestId, exp)
      }
      result.pure[F]
    }
  }

  private[this] def doReplace(request: ReplaceRequest): Either[PatternSyntaxException, List[String]] = {
    Either.catchOnly[PatternSyntaxException] {
      request.dataSource.map(_.replaceAll(request.body.from, request.body.to))
    }
  }

  private[this] def handleError(requestId: String, th: EtlException): TransformationResult = {
    error(s"Error occurred: ${th.getMessage}", th)
    val header = buildResponseHeader(requestId, th)
    TransformationResult(header, List.empty)
  }

}
