package io.github.etl

import java.util.UUID
import cats.effect.{Async, Sync}
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.github.etl.constant.CommonConstant.Operations.{CAPS, REPLACE, WORD_COUNT, WORD_FREQUENCY}
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{EtlResult, EtlSequence, OperationBody}
import io.github.etl.exception.EtlException
import io.github.etl.service.SequenceService.SequenceRequest
import io.github.etl.service.TransformationService.{ReplaceRequest, TransformationResult}
import io.github.etl.service.{AggregationService, SequenceService, TransformationService}
import io.github.etl.util.CommonUtility._
import io.github.etl.util.{LoggerUtility, ResourceReader}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, Headers, HttpRoutes, Response}
import org.typelevel.ci.CIString

object Routes extends LoggerUtility {

  def aggregationRoutes[F[_] : Async](AS: AggregationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / BASE_PATH / AGGREGATE_PATH / WORD_COUNT_PATH =>
        val requestId = extractRequestId(req.headers)
        processRequest(dsl, requestId) { data: List[String] =>
          for {
            wordCount <- AS.wordCount(AggregationService.Count(requestId, data))
            resp <- Ok(wordCount)
          } yield resp
        }
      case req@GET -> Root / BASE_PATH / AGGREGATE_PATH / WORD_FREQUENCY_PATH =>
        val requestId = extractRequestId(req.headers)
        processRequest(dsl, requestId) { data: List[String] =>
          for {
            wordFrequency <- AS.wordFrequency(AggregationService.Frequency(requestId, data))
            resp <- Ok(wordFrequency)
          } yield resp
        }
    }
  }

  def capsTransformationRoutes[F[_] : Async](TS: TransformationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / BASE_PATH / TRANSFORM_PATH / CAPS_PATH =>
        val requestId = extractRequestId(req.headers)
        processRequest(dsl, requestId) { data: List[String] =>
          for {
            result <- TS.caps(TransformationService.CapsRequest(requestId, data))
            resp <- Ok(result)
          } yield resp
        }
    }
  }

  def replaceTransformationRoutes[F[_] : Async](TS: TransformationService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root / BASE_PATH / TRANSFORM_PATH / REPLACE_PATH =>
        implicit val decoder: EntityDecoder[F, OperationBody] = jsonOf[F, OperationBody]
        val requestId = extractRequestId(req.headers)
        req.as[OperationBody].attempt.flatMap {
          case Right(value) =>
            processRequest(dsl, requestId) { data: List[String] =>
              for {
                result <- TS.replace(ReplaceRequest(requestId, OperationBody(value.from, value.to), data))
                resp <- Ok(result)
              } yield resp
            }
          case Left(th) => BadRequest(handleBadRequest(requestId, EtlException(CODE_4000, JSON_ERROR, th)))
        }
    }
  }

  def sequenceRoutes[F[_] : Async](SS: SequenceService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req@POST -> Root / BASE_PATH / SEQUENCE_PATH =>
        implicit val decoder: EntityDecoder[F, EtlSequence] = jsonOf[F, EtlSequence]
        val requestId = extractRequestId(req.headers)
        req.as[EtlSequence].attempt.flatMap {
          case Right(value) if !validateSequenceOperations(value.etl) =>
            BadRequest(handleBadRequest(requestId, EtlException(CODE_4003, OPERATION_ERROR)))
          case Right(value) =>
            processRequest(dsl, requestId) { data: List[String] =>
              val sr = SequenceRequest(requestId, value)
              for {
                capsTransOpt <- SS.applyTransformation(CAPS.toString, sr, data)
                replaceTransOpt <- SS.applyTransformation(REPLACE.toString, sr, extractData(data, capsTransOpt))
                wordCountAggrOpt <- SS.applyAggregation(WORD_COUNT.toString, sr, extractData(data, replaceTransOpt))
                wordFrequencyAggrOpt <- SS.applyAggregation(WORD_FREQUENCY.toString, sr, extractData(data, replaceTransOpt))
                resp <- Ok(Json.obj((ETL_RESPONSE_TEXT, Json.fromValues(processResult(CAPS, capsTransOpt) ++
                  processResult(REPLACE, replaceTransOpt) ++ processResult(WORD_COUNT, wordCountAggrOpt) ++
                  processResult(WORD_COUNT, wordFrequencyAggrOpt)))))
              } yield resp
            }
          case Left(th) => BadRequest(handleBadRequest(requestId, EtlException(CODE_4000, JSON_ERROR, th)))
        }
    }
  }

  def processRequest[F[_] : Async](dsl: Http4sDsl[F], reqId: String)(f: List[String] => F[Response[F]]): F[Response[F]] = {
    import dsl._
    ResourceReader.lines match {
      case Left(th) => BadRequest(handleBadRequest(reqId, th))
      case Right(data) => f(data)
    }
  }

  private[this] def processResult(operation: Operations.Value, resultOpt: Option[EtlResult]): List[Json] = {
    resultOpt.map { result => etlResultToJson(operation.toString, result) }.toList
  }

  private[this] def handleBadRequest(reqId: String, th: EtlException): Json = {
    error(th.getMessage, th)
    Json.obj((HEADER_TEXT, buildResponseHeader(reqId, th).toJson), (RESULT_TEXT, Json.obj()))
  }

  private[this] def extractRequestId(headers: Headers): String = {
    headers.get(CIString(REQUEST_ID_TEXT)).map(_.head.value).getOrElse(UUID.randomUUID().toString)
  }

  private[this] def extractData(data: List[String], capsTransOpt: Option[TransformationResult]): List[String] = {
    capsTransOpt.map(_.result).getOrElse(data)
  }

}
