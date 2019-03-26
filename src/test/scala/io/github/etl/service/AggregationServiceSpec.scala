package io.github.etl.service

import cats.effect.IO
import io.github.etl.exception.EtlException
import io.github.etl.util.{CommonUtility, TestHelper}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class AggregationServiceSpec extends Specification with TestHelper {

  "AggregationService" >> {
    "Word count returns count" >> {
      testWordCount()
    }
    "Word count returns error" >> {
      testWordCountWithException()
    }
    "Word frequency returns frequency" >> {
      testWordFrequency()
    }
    "Word frequency returns error" >> {
      testWordFrequencyWithException()
    }
  }

  private[this] val serviceImplAlg = AggregationService.impl[IO]

  private[this] def testWordCount(): MatchResult[Boolean] = {
    val aggregationResult = serviceImplAlg.wordCount(AggregationService.Count("123", dataSource)).unsafeRunSync()
    aggregationResult.header.requestId must beEqualTo("123")
    aggregationResult.result.isEmpty must beFalse
  }

  private[this] def testWordFrequency(): MatchResult[Boolean] = {
    val aggregationResult = serviceImplAlg.wordFrequency(AggregationService.Frequency("123", dataSource)).unsafeRunSync()
    aggregationResult.header.requestId must beEqualTo("123")
    aggregationResult.result.isEmpty must beFalse
  }

  private[this] val serviceWithException: AggregationService[IO] = new AggregationService[IO] {

    def buildResult(reqId: String): IO[AggregationService.AggregationResult] = {
      val header = CommonUtility.buildResponseHeader(reqId, EtlException())
      IO.pure(AggregationService.AggregationResult(header, Map.empty))
    }

    override def wordCount(count: AggregationService.Count): IO[AggregationService.AggregationResult] = buildResult(count.requestId)

    override def wordFrequency(frequency: AggregationService.Frequency): IO[AggregationService.AggregationResult] = buildResult(frequency.requestId)
  }

  private[this] def testWordCountWithException(): MatchResult[Boolean] = {
    val aggregationResult = serviceWithException.wordCount(AggregationService.Count("123", dataSource)).unsafeRunSync()
    aggregationResult.header.requestId must beEqualTo("123")
    aggregationResult.result.isEmpty must beTrue
  }

  private[this] def testWordFrequencyWithException(): MatchResult[Boolean] = {
    val aggregationResult = serviceWithException.wordFrequency(AggregationService.Frequency("123", dataSource)).unsafeRunSync()
    aggregationResult.header.requestId must beEqualTo("123")
    aggregationResult.result.isEmpty must beTrue
  }

}
