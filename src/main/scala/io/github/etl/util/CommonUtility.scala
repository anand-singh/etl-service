package io.github.etl.util

import java.util.UUID

import io.github.etl.constant.CommonConstant.Operations._
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{Operation, ResponseHeader}
import io.github.etl.exception.EtlException

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

}
