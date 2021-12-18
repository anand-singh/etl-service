package io.github.etl.route

import cats.effect.IO
import io.github.etl.Routes
import io.github.etl.service.AggregationService
import io.github.etl.util.TestHelper
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class AggregationRouteSpec extends Specification with TestHelper {

  "Aggregation routes" >> {
    "Word count return 200" >> {
      wordCountUriReturns200()
    }
    "Word count return 404" >> {
      wordCountUriReturns404()
    }
    "Word frequency return 200" >> {
      wordFrequencyUriReturns200()
    }
    "Word frequency return 404" >> {
      wordFrequencyUriReturns404()
    }
  }

  private[this] val aggregationServiceAlg = AggregationService.impl[IO]

  private[this] val wordCountSuccessResponse: IO[Response[IO]] = {
    val getWordCount = Request[IO](Method.GET, uri"etl/aggregate/wordcount")
    Routes.aggregationRoutes(aggregationServiceAlg).orNotFound(getWordCount)
  }

  private[this] def wordCountUriReturns200(): MatchResult[Boolean] =
    check(wordCountSuccessResponse, Status.Ok, Some("SUCCESS")) must beTrue

  private[this] val wordCountErrorResponse: IO[Response[IO]] = {
    val getWordFrequency = Request[IO](Method.GET, uri"etl/aggregate/wordcount-xyz")
    Routes.aggregationRoutes(aggregationServiceAlg).orNotFound(getWordFrequency)
  }

  private[this] def wordCountUriReturns404(): MatchResult[Boolean] =
    check(wordCountErrorResponse, Status.NotFound, Some("Not found")) must beTrue

  private[this] val wordFrequencySuccessResponse: IO[Response[IO]] = {
    val getWordFrequency = Request[IO](Method.GET, uri"etl/aggregate/wordfrequency")
    Routes.aggregationRoutes(aggregationServiceAlg).orNotFound(getWordFrequency)
  }

  private[this] def wordFrequencyUriReturns200(): MatchResult[Boolean] =
    check(wordFrequencySuccessResponse, Status.Ok, Some("SUCCESS")) must beTrue

  private[this] val wordFrequencyErrorResponse: IO[Response[IO]] = {
    val getWordFrequency = Request[IO](Method.GET, uri"etl/aggregate/wordfrequency-xyz")
    Routes.aggregationRoutes(aggregationServiceAlg).orNotFound(getWordFrequency)
  }

  private[this] def wordFrequencyUriReturns404(): MatchResult[Boolean] =
    check(wordFrequencyErrorResponse, Status.NotFound, Some("Not found")) must beTrue

}
