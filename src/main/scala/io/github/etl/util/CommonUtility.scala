package io.github.etl.util

import java.util.UUID

import io.github.etl.constant.CommonConstant.Operations._
import io.github.etl.constant.CommonConstant._
import io.github.etl.constant.StatusCode._
import io.github.etl.domain.{Operation, ResponseHeader, SequenceRequestData}
import io.github.etl.exception.EtlServiceException

object CommonUtility {

  def buildResponseHeader(requestId: String): ResponseHeader = {
    ResponseHeader(requestId, UUID.randomUUID().toString, CODE_2000, SUCCESS)
  }

  def buildResponseHeader(requestId: String, ex: EtlServiceException): ResponseHeader = {
    ResponseHeader(requestId, UUID.randomUUID().toString, ex.code, ex.getMessage)
  }

  def validateSequenceOperations(operations: List[Operation]): Boolean = {
    validateHeadSequence(operations) && validateFullSequence(operations)
  }

  private[this] def validateFullSequence(operations: List[Operation]): Boolean =
    !operations.map { operation => Operations.getWithName(operation.opr) }.contains(DEFAULT)

  private[this] def validateHeadSequence(operations: List[Operation]): Boolean = {
    operations.headOption.exists { value =>
      Operations.getWithName(value.opr) match {
        case DEFAULT | WORD_COUNT | WORD_FREQUENCY => false
        case _ => true
      }
    }
  }

}
