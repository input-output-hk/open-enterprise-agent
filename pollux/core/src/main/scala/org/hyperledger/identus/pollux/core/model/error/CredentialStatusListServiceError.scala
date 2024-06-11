package org.hyperledger.identus.pollux.core.model.error

import org.hyperledger.identus.shared.models.{Failure, StatusCode}

import java.util.UUID

sealed trait CredentialStatusListServiceError(
    val statusCode: StatusCode,
    val userFacingMessage: String
) extends Failure {
  override val namespace: String = "CredentialStatusListError"
}

object CredentialStatusListServiceError {
  final case class StatusListNotFound(id: UUID)
      extends CredentialStatusListServiceError(
        StatusCode.NotFound,
        s"There is no credential status record matching the provided identifier: id=$id"
      )
}
