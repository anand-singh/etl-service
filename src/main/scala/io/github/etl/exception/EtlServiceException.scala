package io.github.etl.exception

/**
  * ETL Exception - An app level custom exception
  */
final case class EtlServiceException
(
  code: Long = 5000,
  private val message: String = "Internal Server Error",
  private val cause: Throwable = None.orNull
) extends Exception(message, cause)
