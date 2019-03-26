package io.github.etl.exception

import io.github.etl.constant.StatusCode

/**
  * ETL Exception - An app level custom exception
  */
final case class EtlException
(
  code: Long = StatusCode.CODE_5000,
  private val message: String = "Internal Server Error",
  private val cause: Throwable = None.orNull
) extends Exception(message, cause)
