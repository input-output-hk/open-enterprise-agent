package io.iohk.atala.verification.controller.http

import io.iohk.atala.api.http.Annotation
import io.iohk.atala.pollux.core.service.verification.VcVerificationRequest as ServiceVcVerificationRequest
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.{description, encodedExample}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class VcVerificationRequest(
    @description(VcVerificationRequest.annotations.credential.description)
    @encodedExample(VcVerificationRequest.annotations.credential.example)
    credential: String,
    @description(VcVerificationRequest.annotations.vcVerification.description)
    @encodedExample(VcVerificationRequest.annotations.vcVerification.example)
    verifications: List[ParameterizableVcVerification]
)

object VcVerificationRequest {
  object annotations {

    object credential
        extends Annotation[String](
          description = "Encoded Verifiable Credential to verify",
          example =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )

    object vcVerification
        extends Annotation[List[VcVerification]](
          description = "The list of Verifications to verify. All verifications run if Verifications left empty",
          example = List(
            VcVerification.SignatureVerification,
            VcVerification.IssuerIdentification,
            VcVerification.ExpirationCheck,
            VcVerification.NotBeforeCheck,
            VcVerification.AudienceCheck,
            VcVerification.SubjectVerification,
            VcVerification.IntegrityOfClaims,
            VcVerification.ComplianceWithStandards,
            VcVerification.RevocationCheck,
            VcVerification.AlgorithmVerification,
            VcVerification.SchemaCheck,
            VcVerification.SemanticCheckOfClaims,
          )
        )
  }

  given credentialVerificationRequestEncoder: JsonEncoder[VcVerificationRequest] =
    DeriveJsonEncoder.gen[VcVerificationRequest]

  given credentialVerificationRequestDecoder: JsonDecoder[VcVerificationRequest] =
    DeriveJsonDecoder.gen[VcVerificationRequest]

  given credentialVerificationRequestSchema: Schema[VcVerificationRequest] = Schema.derived

  def toService(request: VcVerificationRequest): List[ServiceVcVerificationRequest] = {
    request.verifications.map(verification =>
      ServiceVcVerificationRequest(
        credential = request.credential,
        verification = VcVerification.convert(verification.verification),
        parameter = verification.parameter.map(param => VcVerificationParameter.convert(param))
      )
    )
  }
}
