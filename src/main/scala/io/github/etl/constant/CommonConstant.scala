package io.github.etl.constant

import scala.util.Try

/**
  * App constants
  */
object CommonConstant {

  val BASE_PATH = "etl"

  val AGGREGATE_PATH = "aggregate"
  val WORD_COUNT_PATH = "wordcount"
  val WORD_FREQUENCY_PATH = "wordfrequency"

  val TRANSFORM_PATH = "transform"
  val CAPS_PATH = "caps"
  val REPLACE_PATH = "replace"

  val SEQUENCE_PATH = "sequence"

  val SUCCESS = "SUCCESS"

  val RESOURCE_ERROR = "Resource reading error!"
  val SYNTAX_ERROR = "Pattern syntax error!"
  val JSON_ERROR = "Malformed Json error!"
  val DATA_ERROR = "Mandatory data missing!"
  val OPERATION_ERROR = "Can not sequence non logical operations!"

  val REQUEST_ID_TEXT = "Request-Id"
  val COUNT_TEXT = "count"
  val RESULT_TEXT = "result"
  val HEADER_TEXT = "header"
  val ETL_RESPONSE_TEXT = "etlResponse"

  object Operations extends Enumeration {

    type Operations = Value

    val DEFAULT: Operations.Value = Value("default")
    val CAPS: Operations.Value = Value("caps")
    val REPLACE: Operations.Value = Value("replace")
    val WORD_COUNT: Operations.Value = Value("wordcount")
    val WORD_FREQUENCY: Operations.Value = Value("wordfrequency")

    def getWithName(name: String): Operations.Value = {
     Try(withName(name.toLowerCase)).getOrElse(DEFAULT)
    }

  }

}


