package io.github.etl.route

import cats.effect.IO
import io.circe.Json
import io.github.etl.Routes
import io.github.etl.service.TransformationService
import io.github.etl.util.TestHelper
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class TransformationRouteSpec extends Specification with TestHelper {

  "Transformation routes" >> {
    "caps operation return 200" >> {
      capsUriReturns200()
    }
    "caps operation return 404" >> {
      capsUriReturns404()
    }
    "replace operation return 200" >> {
      replaceUriReturns200()
    }
    "replace operation return 400" >> {
      replaceUriReturns400()
    }
    "replace operation return 404" >> {
      replaceUriReturns404()
    }
  }

  private[this] val transformationServiceAlg = TransformationService.impl[IO]

  private[this] val capsOperationSuccessResponse: IO[Response[IO]] = {
    val capsRequest = Request[IO](Method.GET, uri"etl/transform/caps")
    Routes.capsTransformationRoutes(transformationServiceAlg).orNotFound(capsRequest)
  }

  private[this] def capsUriReturns200(): MatchResult[Boolean] =
    check(capsOperationSuccessResponse, Status.Ok, Some("SUCCESS")) must beTrue

  private[this] val capsOperationErrorResponse: IO[Response[IO]] = {
    val capsNotFoundRequest = Request[IO](Method.GET, uri"etl/transform/caps-xyz")
    Routes.capsTransformationRoutes(transformationServiceAlg).orNotFound(capsNotFoundRequest)
  }

  private[this] def capsUriReturns404(): MatchResult[Boolean] =
    check(capsOperationErrorResponse, Status.NotFound, Some("Not found")) must beTrue

  private[this] val replaceSuccessResponse: IO[Response[IO]] = {
    val body = Json.obj(("from", Json.fromString("this")), ("to", Json.fromString("test")))
    val replaceRequest = Request[IO](Method.POST, uri"etl/transform/replace").withEntity(body)
    Routes.replaceTransformationRoutes(transformationServiceAlg).orNotFound(replaceRequest)
  }

  private[this] def replaceUriReturns200(): MatchResult[Boolean] =
    check(replaceSuccessResponse, Status.Ok, Some("SUCCESS")) must beTrue

  private[this] val replaceBadRequestResponse: IO[Response[IO]] = {
    val body = Json.obj(("from", Json.fromString("this")))
    val replaceBadReqRequest = Request[IO](Method.POST, uri"etl/transform/replace").withEntity(body)
    Routes.replaceTransformationRoutes(transformationServiceAlg).orNotFound(replaceBadReqRequest)
  }

  private[this] def replaceUriReturns400(): MatchResult[Boolean] =
    check(replaceBadRequestResponse, Status.BadRequest, Some("Malformed Json error!")) must beTrue

  private[this] val replaceErrorResponse: IO[Response[IO]] = {
    val replaceNotFoundRequest = Request[IO](Method.POST, uri"etl/transform/replace-xyz")
    Routes.replaceTransformationRoutes(transformationServiceAlg).orNotFound(replaceNotFoundRequest)
  }

  private[this] def replaceUriReturns404(): MatchResult[Boolean] =
    check(replaceErrorResponse, Status.NotFound, Some("Not found")) must beTrue

}
