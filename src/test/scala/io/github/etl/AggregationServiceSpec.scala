package io.github.etl

import cats.effect.IO
import io.github.etl.service.AggregationService
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class AggregationServiceSpec extends org.specs2.mutable.Specification {

  "AggregationService" >> {
    "return 200" >> {
      uriReturns200()
    }
    "return word count" >> {
      uriReturnsWordCount()
    }
  }

  private[this] val retAggregation: Response[IO] = {
    val getWordCount = Request[IO](Method.GET, Uri.uri("etl/aggregate/wordcount"))
    val aggregationServiceAlg = AggregationService.impl[IO]
    Routes.aggregationRoutes(aggregationServiceAlg).orNotFound(getWordCount).unsafeRunSync()
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retAggregation.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsWordCount(): MatchResult[String] =
    retAggregation.as[String].unsafeRunSync() must contain("count")
}