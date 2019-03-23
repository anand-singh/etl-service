package io.github.etl.service

import java.util.regex.PatternSyntaxException

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import io.github.etl.domain.ResponseHeader
import io.github.etl.exception.EtlServiceException
import io.github.etl.util.CommonUtility._
import io.github.etl.util.{LoggerUtility, ResourceReader}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import io.github.etl.constant.StatusCode._
import io.github.etl.constant.CommonConstant._

/**
  * TransformationService - Supports caps & replace operations
  */
trait TransformationService[F[_]] {

  def caps(request: TransformationService.CapsRequest): F[TransformationService.TransformationResult]

  def replace(request: TransformationService.ReplaceRequest): F[TransformationService.TransformationResult]

}

object TransformationService extends LoggerUtility {

  implicit def apply[F[_]](implicit ev: TransformationService[F]): TransformationService[F] = ev

  final case class CapsRequest(requestId: String) extends AnyVal

  final case class ReplaceRequest(requestId: String, from: String, to: String)

  final case class TransformationResult(header: ResponseHeader, result: List[String])

  object TransformationResult {
    implicit val transResultEncoder: Encoder[TransformationResult] = (a: TransformationResult) => Json.obj(
      ("header", a.header.toJson),
      ("result", Json.fromString(a.result.mkString(" ")))
    )

    implicit def transResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, TransformationResult] =
      jsonEncoderOf[F, TransformationResult]
  }

  def impl[F[_] : Applicative]: TransformationService[F] = new TransformationService[F] {
    def caps(request: TransformationService.CapsRequest): F[TransformationService.TransformationResult] = {
      val result = ResourceReader.lines match {
        case Right(value) =>
          val header = buildResponseHeader(request.requestId)
          TransformationResult(header, value.map(_.toUpperCase))
        case Left(th) => handleError(request.requestId, th)
      }
      result.pure[F]
    }

    def replace(request: TransformationService.ReplaceRequest): F[TransformationService.TransformationResult] = {
      val replacedData = for {
        lines <- ResourceReader.lines.right
        result <- doReplace(lines, request).right
      } yield result
      val result = replacedData match {
        case Right(value) =>
          val header = buildResponseHeader(request.requestId)
          TransformationResult(header, value)
        case Left(th: PatternSyntaxException) =>
          val exp = EtlServiceException(CODE_3000, SYNTAX_ERROR, th)
          handleError(request.requestId, exp)
        case Left(th: EtlServiceException) => handleError(request.requestId, th)
        case Left(_: Exception) => handleError(request.requestId, EtlServiceException())
      }
      result.pure[F]
    }
  }

  private def handleError(requestId: String, th: EtlServiceException): TransformationResult = {
    error(th.getMessage, th)
    val header = buildResponseHeader(requestId, th)
    TransformationResult(header, List.empty)
  }

  private def doReplace(data: List[String], request: TransformationService.ReplaceRequest)
  : Either[PatternSyntaxException, List[String]] = {
    Either.catchOnly[PatternSyntaxException](data.map(_.replaceAll(request.from, request.to)))
  }

}
