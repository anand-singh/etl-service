package io.github.etl.route

import cats.effect.IO
import io.circe.Json
import io.github.etl.Routes
import io.github.etl.service.{AggregationService, SequenceService, TransformationService}
import io.github.etl.util.TestHelper
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class SequenceRouteSpec extends Specification with TestHelper {

  "Sequence routes" >> {
    "caps sequence operation return 200" >> {
      sequenceUriForCapsOperationReturns200()
    }
    "replace sequence operation return 200" >> {
      sequenceUriForReplaceOperationReturns200()
    }
    "replace sequence operation return 400" >> {
      sequenceUriForReplaceOperationReturns400()
    }
  }

  private[this] val aggregationServiceAlg = AggregationService.impl[IO]
  private[this] val transformationServiceAlg = TransformationService.impl[IO]

  private[this] val sequenceServiceAlg = SequenceService.impl[IO](transformationServiceAlg, aggregationServiceAlg)

  private[this] val capsOperationSuccessResponse: IO[Response[IO]] = {
    val body = Json.obj(
      ("etl", Json.fromValues(List(
        Json.obj(
          ("opr", Json.fromString("caps"))
        )
      )))
    )
    val request = Request[IO](Method.POST, Uri.uri("etl/sequence")).withEntity(body)
    Routes.sequenceRoutes(sequenceServiceAlg).orNotFound(request)
  }

  private[this] def sequenceUriForCapsOperationReturns200(): MatchResult[Boolean] =
    check(capsOperationSuccessResponse, Status.Ok, Some("")) must beTrue

  private[this] val replaceOperationSuccessResponse: IO[Response[IO]] = {
    val body = Json.obj(
      ("etl", Json.fromValues(List(
        Json.obj(
          ("opr", Json.fromString("replace")),
          ("body", Json.obj(("from", Json.fromString("this")), ("to", Json.fromString("test"))))
        )
      )))
    )
    val request = Request[IO](Method.POST, Uri.uri("etl/sequence")).withEntity(body)
    Routes.sequenceRoutes(sequenceServiceAlg).orNotFound(request)
  }

  private[this] def sequenceUriForReplaceOperationReturns200(): MatchResult[Boolean] =
    check(replaceOperationSuccessResponse, Status.Ok, Some("")) must beTrue

  private[this] val replaceOperationErrorResponse: IO[Response[IO]] = {
    val body = Json.obj(
      ("etl", Json.fromValues(List(Json.obj(("opr", Json.fromString("replace"))))))
    )
    val request = Request[IO](Method.POST, Uri.uri("etl/sequence")).withEntity(body)
    Routes.sequenceRoutes(sequenceServiceAlg).orNotFound(request)
  }

  private[this] def sequenceUriForReplaceOperationReturns400(): MatchResult[Boolean] =
    check(replaceOperationErrorResponse, Status.BadRequest, Some("")) must beTrue


}
