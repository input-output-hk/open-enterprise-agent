package org.hyperledger.identus.shared.models

trait Failure {
  val namespace: String
  val statusCode: StatusCode
  val userFacingMessage: String
}

sealed class StatusCode(val code: Int)

object StatusCode {
  val BadRequest: StatusCode = StatusCode(400)
  val NotFound: StatusCode = StatusCode(404)
  val UnprocessableContent: StatusCode = StatusCode(422)

  val InternalServerError: StatusCode = StatusCode(500)
  val BadGateway: StatusCode = StatusCode(502)
}
