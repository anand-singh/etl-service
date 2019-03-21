package io.github.etl

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.github.etl.service.WordCountService
import org.http4s.{Headers, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString

object Routes {

  def wordCountRoutes[F[_] : Sync](WC: WordCountService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root / "etl" / "aggregate" / "wordcount" =>
        val requestId = extractRequestId(req.headers)
        for {
          wordCount <- WC.wordCount(WordCountService.Count(requestId))
          resp <- Ok(wordCount)
        } yield resp
      case req@POST -> Root / "etl" / "aggregate" / "wordcount" =>
        val requestId = UUID.randomUUID().toString
        for {
          wordCount <- WC.wordCount(WordCountService.Count(requestId))
          resp <- Ok(wordCount)
        } yield resp
    }
  }

  private def extractRequestId(headers: Headers): String = {
    headers.get(CaseInsensitiveString("Request-Id"))
      .map { header => header.value }
      .getOrElse(UUID.randomUUID().toString)
  }

}