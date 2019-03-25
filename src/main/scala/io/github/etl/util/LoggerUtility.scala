package io.github.etl.util

import org.slf4j.{Logger, LoggerFactory}

/**
  * Logger helper
  *
  * @author Anand Singh
  */
trait LoggerUtility {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  protected def debug(message: String): Unit = logger.debug(message)

  protected def info(message: String): Unit = logger.info(message)

  protected def warn(message: String): Unit = logger.warn(message)

  protected def error(message: String): Unit = logger.error(message)

  protected def error(message: String, exception: Throwable): Unit = logger.error(message, exception)

}
