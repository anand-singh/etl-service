package io.github.etl.domain

/**
  * All request data container
  */

final case class ReplaceRequestData(from: String, to: String)

final case class SequenceRequestData(etl: List[Operation])

final case class Operation(opr: String, body: Option[ReplaceRequestData])