package io.github.etl

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.github.etl.constant.CommonConstant.Operations.{CAPS, REPLACE, WORD_COUNT, WORD_FREQUENCY}
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{EtlSequence, OperationBody}
import io.github.etl.exception.EtlException
import io.github.etl.service.AggregationService.AggregationResult
import io.github.etl.service.SequenceService.SequenceRequest
import io.github.etl.service.TransformationService.{ReplaceRequest, TransformationResult}
import io.github.etl.service.{AggregationService, SequenceService, TransformationService}
import io.github.etl.util.CommonUtility._
import io.github.etl.util.{LoggerUtility, ResourceReader}
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
        ResourceReader.lines match {
          case Left(th) => BadRequest(handleBadRequest(requestId, th))
          case Right(data) => for {
            wordCount <- AS.wordCount(AggregationService.Count(requestId, data))
            resp <- Ok(wordCount)
          } yield resp
        }
      case req@GET -> Root / "etl" / "aggregate" / "wordfrequency" =>
        val requestId = extractRequestId(req.headers)
        ResourceReader.lines match {
          case Left(th) => BadRequest(handleBadRequest(requestId, th))
          case Right(data) => for {
            wordFrequency <- AS.wordFrequency(AggregationService.Frequency(requestId, data))
            resp <- Ok(wordFrequency)
          } yield resp
        }
    }
  }

  def capsTransformationRoutes[F[_] : Sync](TS: TransformationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "etl" / "transform" / "caps" =>
        val requestId = extractRequestId(req.headers)
        ResourceReader.lines match {
          case Left(th) => BadRequest(handleBadRequest(requestId, th))
          case Right(data) =>
            for {
              result <- TS.caps(TransformationService.CapsRequest(requestId, data))
              resp <- Ok(result)
            } yield resp
        }
    }
  }

  def replaceTransformationRoutes[F[_] : Sync](TS: TransformationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root / "etl" / "transform" / "replace" =>
        implicit val decoder: EntityDecoder[F, OperationBody] = jsonOf[F, OperationBody]
        val requestId = extractRequestId(req.headers)
        req.as[OperationBody].attempt.flatMap {
          case Right(value) =>
            ResourceReader.lines match {
              case Left(th) => BadRequest(handleBadRequest(requestId, th))
              case Right(data) =>
                for {
                  result <- TS.replace(ReplaceRequest(requestId, OperationBody(value.from, value.to), data))
                  resp <- Ok(result)
                } yield resp
            }
          case Left(th) =>
            val etlServiceException = EtlException(CODE_4000, JSON_ERROR, th)
            BadRequest(handleBadRequest(requestId, etlServiceException))
        }
    }
  }

  def sequenceRoutes[F[_] : Sync](SS: SequenceService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req@POST -> Root / "etl" / "sequence" =>
        implicit val decoder: EntityDecoder[F, EtlSequence] = jsonOf[F, EtlSequence]
        val requestId = extractRequestId(req.headers)
        req.as[EtlSequence].attempt.flatMap {
          case Right(value) if !validateSequenceOperations(value.etl) =>
            val etlServiceException = EtlException(CODE_4003, OPERATION_ERROR)
            BadRequest(handleBadRequest(requestId, etlServiceException))
          case Right(value) =>
            ResourceReader.lines match {
              case Left(th) => BadRequest(handleBadRequest(requestId, th))
              case Right(data) =>
                val sequenceRequest = SequenceRequest(requestId, value)
                for {
                  capsTransOpt <- SS.applyTransformation(CAPS.toString, sequenceRequest, data)
                  replaceTransOpt <- SS.applyTransformation(REPLACE.toString, sequenceRequest, capsTransOpt.map(_.result).getOrElse(data))
                  wordCountAggrOpt <- SS.applyAggregation(WORD_COUNT.toString, sequenceRequest, replaceTransOpt.map(_.result).getOrElse(data))
                  wordFrequencyAggrOpt <- SS.applyAggregation(WORD_FREQUENCY.toString, sequenceRequest, replaceTransOpt.map(_.result).getOrElse(data))
                  resp <- Ok(Json.obj(
                    ("etlResponse", Json.fromValues(
                      processTransResult(CAPS, capsTransOpt) ++
                        processTransResult(REPLACE, replaceTransOpt) ++
                        processAggrResult(WORD_COUNT, wordCountAggrOpt) ++
                        processAggrResult(WORD_COUNT, wordFrequencyAggrOpt)
                    ))
                  ))
                } yield resp
            }
          case Left(th) =>
            val etlServiceException = EtlException(CODE_4000, JSON_ERROR, th)
            BadRequest(handleBadRequest(requestId, etlServiceException))
        }
    }
  }

  private[this] def processAggrResult(operation: Operations.Value,
                                      aggrResultOpt: Option[AggregationResult]): List[Json] = {
    aggrResultOpt.map { aggrResult =>
      aggregationResultToJson(operation.toString, aggrResult)
    }.toList
  }

  private[this] def processTransResult(operation: Operations.Value,
                                       transResultOpt: Option[TransformationResult]): List[Json] = {
    transResultOpt.map { transResult =>
      transformationResultToJson(operation.toString, transResult)
    }.toList
  }

  private[this] def handleBadRequest(reqId: String, th: EtlException): Json = {
    error(th.getMessage, th)
    val header = buildResponseHeader(reqId, th)
    Json.obj(("header", header.toJson), ("result", Json.obj()))
  }

  private[this] def extractRequestId(headers: Headers): String = {
    headers.get(CaseInsensitiveString("Request-Id"))
      .map { header => header.value }
      .getOrElse(UUID.randomUUID().toString)
  }

}
