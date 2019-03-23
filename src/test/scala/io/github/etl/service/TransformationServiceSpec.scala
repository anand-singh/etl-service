package io.github.etl.service

import cats.effect.IO
import io.github.etl.exception.EtlServiceException
import io.github.etl.util.{CommonUtility, TestHelper}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class TransformationServiceSpec extends Specification with TestHelper {

  "TransformationService" >> {
    "caps operation returns capital letter result" >> {
      testCapsOperation()
    }
    "caps operation returns error" >> {
      testCpasOperationWithException()
    }
    "replace operation returns replaced result" >> {
      testReplaceOperation()
    }
    "replace operation returns error on wrong PatternSyntax" >> {
      testReplaceOperationWithPatternSyntaxException()
    }
    "replace operation returns error" >> {
      testReplaceOperationWithException()
    }
  }

  private[this] val serviceImplAlg = TransformationService.impl[IO]

  private[this] def testCapsOperation(): MatchResult[String] = {
    val transformationResult = serviceImplAlg.caps(TransformationService.CapsRequest("123")).unsafeRunSync()
    transformationResult.header.requestId must beEqualTo("123")
    transformationResult.header.statusMessage must beEqualTo("SUCCESS")
  }

  private[this] def testReplaceOperation(): MatchResult[Boolean] = {
    val transformationResult = serviceImplAlg.replace(TransformationService.ReplaceRequest("123", "this", "test")).unsafeRunSync()
    transformationResult.header.requestId must beEqualTo("123")
    transformationResult.result.mkString.contains("test") must beTrue
  }

  private[this] def testReplaceOperationWithPatternSyntaxException(): MatchResult[Boolean] = {
    val transformationResult = serviceImplAlg.replace(TransformationService.ReplaceRequest("123", "[", "test")).unsafeRunSync()
    transformationResult.header.requestId must beEqualTo("123")
    transformationResult.header.statusMessage must beEqualTo("Pattern syntax error!")
    transformationResult.result.isEmpty must beTrue
  }

  private[this] val serviceWithException: TransformationService[IO] = new TransformationService[IO] {

    def buildResult(reqId: String): IO[TransformationService.TransformationResult] = {
      val header = CommonUtility.buildResponseHeader(reqId, EtlServiceException())
      IO.pure(TransformationService.TransformationResult(header, List.empty))
    }

    override def caps(request: TransformationService.CapsRequest): IO[TransformationService.TransformationResult] = buildResult(request.requestId)

    override def replace(request: TransformationService.ReplaceRequest): IO[TransformationService.TransformationResult] = buildResult(request.requestId)
  }

  private[this] def testCpasOperationWithException(): MatchResult[Boolean] = {
    val transformationResult = serviceWithException.caps(TransformationService.CapsRequest("123")).unsafeRunSync()
    transformationResult.header.requestId must beEqualTo("123")
    transformationResult.header.statusMessage must beEqualTo("Internal Server Error")
    transformationResult.result.isEmpty must beTrue
  }

  private[this] def testReplaceOperationWithException(): MatchResult[Boolean] = {
    val transformationResult = serviceWithException.replace(TransformationService.ReplaceRequest("123", "from", "to")).unsafeRunSync()
    transformationResult.header.requestId must beEqualTo("123")
    transformationResult.header.statusMessage must beEqualTo("Internal Server Error")
    transformationResult.result.isEmpty must beTrue
  }

}
