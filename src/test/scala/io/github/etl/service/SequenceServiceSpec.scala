package io.github.etl.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.etl.constant.CommonConstant.Operations
import io.github.etl.constant.StatusCode
import io.github.etl.domain.{EtlSequence, Operation, OperationBody}
import io.github.etl.util.TestHelper
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class SequenceServiceSpec extends Specification with TestHelper {

  "AggregationService" >> {
    "Word count operation returns count" >> {
      testWordCountOperation()
    }
    "Word frequency operation returns frequency" >> {
      testWordFrequencyOperation()
    }
    "Caps operation returns capital letter result " >> {
      testCapsTransformationOperation()
    }
    "Replace operation returns replaced result" >> {
      testReplaceTransformationOperation()
    }
    "Replace operation returns error on empty body" >> {
      testReplaceTransformationOperationWithEmptyBody()
    }
    "Aggregation on default operation" >> {
      testAggregationWithDefaultOperation()
    }
    "Transformation on default operation" >> {
      testTransformationWithDefaultOperation()
    }
  }

  private[this] val aggrServiceImplAlg = AggregationService.impl[IO]
  private[this] val transServiceImplAlg = TransformationService.impl[IO]

  private[this] val serviceImplAlg = SequenceService.impl[IO](transServiceImplAlg, aggrServiceImplAlg)

  val etl = EtlSequence(
    List(Operation(Operations.CAPS.toString, None), Operation(Operations.WORD_COUNT.toString, None))
  )

  private[this] def testWordCountOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.WORD_COUNT.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val aggregationResult = serviceImplAlg.applyAggregation(Operations.WORD_COUNT.toString, request, dataSource).unsafeRunSync()
    aggregationResult.isDefined must beTrue
    aggregationResult.get.header.requestId must beEqualTo("123")
    aggregationResult.get.result.isEmpty must beFalse
  }

  private[this] def testWordFrequencyOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.WORD_FREQUENCY.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val aggregationResult = serviceImplAlg.applyAggregation(Operations.WORD_FREQUENCY.toString, request, dataSource).unsafeRunSync()
    aggregationResult.isDefined must beTrue
    aggregationResult.get.header.requestId must beEqualTo("123")
    aggregationResult.get.result.isEmpty must beFalse
  }

  private[this] def testCapsTransformationOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.CAPS.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val transformationResult = serviceImplAlg.applyTransformation(Operations.CAPS.toString, request, dataSource).unsafeRunSync()
    transformationResult.isDefined must beTrue
    transformationResult.get.header.requestId must beEqualTo("123")
    transformationResult.get.result.isEmpty must beFalse
  }

  private[this] def testReplaceTransformationOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.REPLACE.toString, Some(OperationBody("a", "b")))))
    val request = SequenceService.SequenceRequest("123", etl)
    val transformationResult = serviceImplAlg.applyTransformation(Operations.REPLACE.toString, request, dataSource).unsafeRunSync()
    transformationResult.isDefined must beTrue
    transformationResult.get.header.requestId must beEqualTo("123")
    transformationResult.get.result.isEmpty must beFalse
  }

  private[this] def testReplaceTransformationOperationWithEmptyBody(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.REPLACE.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val transformationResult = serviceImplAlg.applyTransformation(Operations.REPLACE.toString, request, dataSource).unsafeRunSync()
    transformationResult.isDefined must beTrue
    transformationResult.get.header.requestId must beEqualTo("123")
    transformationResult.get.header.statusCode must beEqualTo(StatusCode.CODE_4001)
    transformationResult.get.result.isEmpty must beTrue
  }

  private[this] def testAggregationWithDefaultOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.DEFAULT.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val aggregationResult = serviceImplAlg.applyAggregation(Operations.WORD_COUNT.toString, request, dataSource).unsafeRunSync()
    aggregationResult.isDefined must beFalse
  }

  private[this] def testTransformationWithDefaultOperation(): MatchResult[Boolean] = {
    val etl = EtlSequence(List(Operation(Operations.DEFAULT.toString, None)))
    val request = SequenceService.SequenceRequest("123", etl)
    val transformationResult = serviceImplAlg.applyTransformation(Operations.CAPS.toString, request, dataSource).unsafeRunSync()
    transformationResult.isDefined must beFalse
  }

}
