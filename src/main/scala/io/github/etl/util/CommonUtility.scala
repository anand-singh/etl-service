package io.github.etl.util

import java.util.UUID

import io.circe.Json
import io.github.etl.constant.CommonConstant.Operations._
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{Operation, ResponseHeader}
import io.github.etl.exception.EtlException
import io.github.etl.service.AggregationService.AggregationResult
import io.github.etl.service.TransformationService.TransformationResult

object CommonUtility extends LoggerUtility {

  def buildResponseHeader(requestId: String): ResponseHeader = {
    ResponseHeader(requestId, UUID.randomUUID().toString, CODE_2000, SUCCESS)
  }

  def buildResponseHeader(requestId: String, ex: EtlException): ResponseHeader = {
    ResponseHeader(requestId, UUID.randomUUID().toString, ex.code, ex.getMessage)
  }

  def validateSequenceOperations(operations: List[Operation]): Boolean = {
    val headOperationValidation = operations.headOption.exists { value =>
      Operations.getWithName(value.opr) match {
        case WORD_COUNT | WORD_FREQUENCY => false
        case _ => true
      }
    }

    val operationNotFound = operations.map { operation =>
      Operations.getWithName(operation.opr)
    }.contains(DEFAULT)

    val replaceDataValidation = operations.exists { operation =>
      Operations.getWithName(operation.opr) == REPLACE && operation.body.isEmpty
    }

    info(s"Con-1: $operationNotFound, Con-2: $headOperationValidation, Con-3: $replaceDataValidation, ")

    (operationNotFound, headOperationValidation, replaceDataValidation) match {
      case (true, _, _) => false
      case (_, _, true) => false
      case (false, true, false) => true
      case (_, _, _) => false
    }
  }

  def aggregationResultToJson(operation: String, aggregationResult: AggregationResult): Json = {
    Json.obj(
      ("header", aggregationResult.header.toJson),
      (operation.toString, mapToJson(aggregationResult.result))
    )
  }

  def transformationResultToJson(operation: String, tr: TransformationResult): Json = {
    Json.obj(
      ("header", tr.header.toJson),
      (operation.toString, Json.fromValues(tr.result.map(Json.fromString)))
    )
  }

  private[this] def mapToJson(dataMap: Map[String, Int]): Json = {
    Json.fromValues(dataMap.map { case (key, value) =>
      Json.obj((key, Json.fromInt(value)))
    })
  }

}
