package io.github.etl.domain

/**
  * All request data container
  */

final case class OperationBody(from: String, to: String)

final case class EtlSequence(etl: List[Operation])

final case class Operation(opr: String, body: Option[OperationBody])
