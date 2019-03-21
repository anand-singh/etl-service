package io.github.etl.exception

/**
  * ETL Exception - An app level custom exception
  *
  * @author Anand Singh
  */
final case class EtlServiceException
(
  code: Long = 5000,
  private val message: String = "",
  private val cause: Throwable = None.orNull
) extends Exception(message, cause)
