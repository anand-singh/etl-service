package io.github.etl.constant

object StatusCode {

  val CODE_2000 = 2000 // Success status code

  val CODE_5000 = 5000 // Unexpected errors
  val CODE_5001 = 5001 // System unavailable
  val CODE_5002 = 5002 // Scheduled downtime

  val CODE_4000 = 4000 // Invalid request - Malformed Json
  val CODE_4001 = 4001 // Invalid request - Mandatory data missing
  val CODE_4002 = 4002 // Invalid request - Pattern Syntax error
  val CODE_4003 = 4003 // Invalid request - Non logical operations.

  val CODE_3000 = 3000 // Resource reader errors

}
