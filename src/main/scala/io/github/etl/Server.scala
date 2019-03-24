package io.github.etl

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import io.github.etl.service.{AggregationService, SequenceService, TransformationService}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import cats.implicits._
import org.http4s.implicits._

object Server {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    val aggregationServiceAlg = AggregationService.impl[F]
    val transformationServiceAlg = TransformationService.impl[F]
    val sequenceServiceAlg = SequenceService.impl[F](transformationServiceAlg, aggregationServiceAlg)

    // Combine Service Routes into an HttpApp
    // Can also be done via a Router if you
    // want to extract a segments not checked
    // in the underlying routes.
    val httpApp = (
        Routes.aggregationRoutes[F](aggregationServiceAlg) <+>
        Routes.transformationRoutes[F](transformationServiceAlg) <+>
        Routes.sequenceRoutes[F](sequenceServiceAlg)
      ).orNotFound


    // With Middleware's in place
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .serve
  }.drain
}