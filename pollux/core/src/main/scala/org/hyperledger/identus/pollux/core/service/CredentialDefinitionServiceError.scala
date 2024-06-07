package org.hyperledger.identus.pollux.core.service

import org.hyperledger.identus.pollux.core.model.error.CredentialSchemaError
import org.hyperledger.identus.pollux.core.model.schema.CredentialSchema
import org.hyperledger.identus.pollux.core.model.schema.CredentialSchema.*
import org.hyperledger.identus.shared.models.{Failure, StatusCode}

import java.util.UUID

sealed trait CredentialDefinitionServiceError(
    val statusCode: StatusCode,
    val userFacingMessage: String
) extends Failure {
  override val namespace = "CredentialDefinition"
}

final case class CredentialDefinitionGuidNotFoundError(guid: UUID)
    extends CredentialDefinitionServiceError(
      StatusCode.NotFound,
      s"Credential Definition record cannot be found by `guid`=$guid"
    )

final case class CredentialDefinitionUpdateError(id: UUID, version: String, author: String, message: String)
    extends CredentialDefinitionServiceError(
      StatusCode.BadRequest,
      s"Credential Definition update error: id=$id, version=$version, author=$author, msg=$message"
    )

final case class CredentialDefinitionCreationError(msg: String)
    extends CredentialDefinitionServiceError(
      StatusCode.BadRequest,
      s"Credential Definition Creation Error=${msg}"
    )

final case class CredentialDefinitionValidationError(cause: CredentialSchemaError)
    extends CredentialDefinitionServiceError(
      StatusCode.BadRequest,
      s"Credential Definition Validation Error=${cause.message}"
    )
