package io.github.etl

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{ReplaceRequestData, SequenceRequestData}
import io.github.etl.exception.EtlServiceException
import io.github.etl.service.SequenceService.SequenceRequest
import io.github.etl.service.TransformationService.ReplaceRequest
import io.github.etl.service.{AggregationService, SequenceService, TransformationService}
import io.github.etl.util.{CommonUtility, LoggerUtility}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Headers, HttpRoutes}

object Routes extends LoggerUtility {

  def aggregationRoutes[F[_] : Sync](AS: AggregationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "etl" / "aggregate" / "wordcount" =>
        val requestId = extractRequestId(req.headers)
        for {
          wordCount <- AS.wordCount(AggregationService.Count(requestId))
          resp <- Ok(wordCount)
        } yield resp
      case req@GET -> Root / "etl" / "aggregate" / "wordfrequency" =>
        val requestId = extractRequestId(req.headers)
        for {
          wordFrequency <- AS.wordFrequency(AggregationService.Frequency(requestId))
          resp <- Ok(wordFrequency)
        } yield resp
    }
  }

  def transformationRoutes[F[_] : Sync](TS: TransformationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "etl" / "transform" / "caps" =>
        val requestId = extractRequestId(req.headers)
        for {
          result <- TS.caps(TransformationService.CapsRequest(requestId))
          resp <- Ok(result)
        } yield resp
      case req@POST -> Root / "etl" / "transform" / "replace" =>
        implicit val decoder: EntityDecoder[F, ReplaceRequestData] = jsonOf[F, ReplaceRequestData]
        val requestId = extractRequestId(req.headers)
        req.as[ReplaceRequestData].attempt.flatMap {
          case Right(value) => for {
            result <- TS.replace(ReplaceRequest(requestId, value.from, value.to))
            resp <- Ok(result)
          } yield resp
          case Left(th) =>
            val etlServiceException = EtlServiceException(CODE_4000, JSON_ERROR, th)
            BadRequest(handleBadRequest(requestId, etlServiceException))
        }
    }
  }

  def sequenceRoutes[F[_] : Sync](SS: SequenceService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root / "etl" / "sequence" =>
        implicit val decoder: EntityDecoder[F, SequenceRequestData] = jsonOf[F, SequenceRequestData]
        val requestId = extractRequestId(req.headers)
        req.as[SequenceRequestData].attempt.flatMap {
          case Right(value) =>
            for {
              validationResp <- SS.validateSequence(SequenceRequest(requestId, value))
              sResult <- validationResp.header.statusCode match {
                case CODE_2000 => SS.executeSequence(SequenceRequest(requestId, value))
                case _ => validationResp.pure[F]
              }
              resp <- Ok(sResult)
            } yield resp
          case Left(th) =>
            val etlServiceException = EtlServiceException(CODE_4000, JSON_ERROR, th)
            BadRequest(handleBadRequest(requestId, etlServiceException))
        }
    }
  }

  private[this] def handleBadRequest(reqId: String, th: EtlServiceException): Json = {
    error(th.getMessage, th)
    val header = CommonUtility.buildResponseHeader(reqId, th)
    Json.obj(
      ("header", header.toJson),
      ("result", Json.obj())
    )
  }

  private[this] def extractRequestId(headers: Headers): String = {
    headers.get(CaseInsensitiveString("Request-Id"))
      .map { header => header.value }
      .getOrElse(UUID.randomUUID().toString)
  }

}