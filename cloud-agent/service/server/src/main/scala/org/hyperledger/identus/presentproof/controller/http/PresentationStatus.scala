package org.hyperledger.identus.presentproof.controller.http

import org.hyperledger.identus.api.http.{Annotation, ErrorResponse}
import org.hyperledger.identus.mercury.model.Base64
import org.hyperledger.identus.pollux.core.model.PresentationRecord
import org.hyperledger.identus.presentproof.controller.http.PresentationStatus.annotations
import org.hyperledger.identus.shared.models.{FailureInfo, StatusCode}
import sttp.tapir.{Schema, Validator}
import sttp.tapir.Schema.annotations.{description, encodedExample, validate}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class PresentationStatus(
    @description(annotations.presentationId.description)
    @encodedExample(annotations.presentationId.example)
    presentationId: String,
    @description(annotations.thid.description)
    @encodedExample(annotations.thid.example)
    thid: String,
    @description(annotations.role.description)
    @encodedExample(annotations.role.example)
    @validate(annotations.role.validator)
    role: String,
    @description(annotations.status.description)
    @encodedExample(annotations.status.example)
    @validate(annotations.status.validator)
    status: String,
    @description(annotations.proofs.description)
    @encodedExample(annotations.proofs.example)
    proofs: Seq[ProofRequestAux],
    @description(annotations.data.description)
    @encodedExample(annotations.data.example)
    data: Seq[String],
    @description(annotations.connectionId.description)
    @encodedExample(annotations.connectionId.example)
    connectionId: Option[String] = None,
    @description(annotations.goalcode.description)
    @encodedExample(annotations.goalcode.example)
    goalCode: Option[String] = None,
    @description(annotations.goal.description)
    @encodedExample(annotations.goal.example)
    goal: Option[String] = None,
    @description(annotations.myDid.description)
    @encodedExample(annotations.myDid.example)
    myDid: Option[String] = None,
    @description(annotations.invitation.description)
    invitation: Option[OOBPresentationInvitation] = None,
    @description(annotations.metaRetries.description)
    @encodedExample(annotations.metaRetries.example)
    metaRetries: Int,
    @description(annotations.metaLastFailure.description)
    @encodedExample(annotations.metaLastFailure.example)
    metaLastFailure: Option[ErrorResponse] = None
)

object PresentationStatus {
  def fromDomain(domain: PresentationRecord): PresentationStatus = {
    val data = domain.presentationData match
      case Some(p) =>
        p.attachments.head.data match {
          case Base64(data) =>
            val base64Decoded = new String(java.util.Base64.getUrlDecoder.decode(data))
            Seq(base64Decoded)
          case any => ???
        }
      case None => Seq.empty
    PresentationStatus(
      domain.id.value,
      thid = domain.thid.value,
      role = domain.role.toString,
      status = domain.protocolState.toString,
      proofs = Seq.empty,
      data = data,
      connectionId = domain.connectionId,
      invitation = domain.invitation.map(invitation => OOBPresentationInvitation.fromDomain(invitation)),
      goalCode = domain.invitation.flatMap(_.body.goal_code),
      goal = domain.invitation.flatMap(_.body.goal),
      myDid = domain.invitation.map(_.from.value),
      metaRetries = domain.metaRetries,
      metaLastFailure = domain.metaLastFailure.map(failure => ErrorResponse.failureToErrorResponseConversion(failure)),
    )
  }

  given Conversion[PresentationRecord, PresentationStatus] = fromDomain

  object annotations {
    object presentationId
        extends Annotation[String](
          description = "The unique identifier of the presentation record.",
          example = "3c6d9fa5-d277-431e-a6cb-d3956e47e610"
        )

    object thid
        extends Annotation[String](
          description = "The unique identifier of the thread this presentation record belongs to. " +
            "The value will identical on both sides of the presentation flow (verifier and prover)",
          example = "0527aea1-d131-3948-a34d-03af39aba8b4"
        )
    object role
        extends Annotation[String](
          description = "The role played by the Prism agent in the proof presentation flow.",
          example = "Verifier",
          validator = Validator.enumeration(
            List(
              "Verifier",
              "Prover"
            )
          )
        )
    object status
        extends Annotation[String](
          description = "The current state of the proof presentation record.",
          example = "RequestPending",
          validator = Validator.enumeration(
            List(
              "RequestPending",
              "RequestSent",
              "RequestReceived",
              "RequestRejected",
              "PresentationPending",
              "PresentationGenerated",
              "PresentationSent",
              "PresentationReceived",
              "PresentationVerified",
              "PresentationVerificationFailed",
              "PresentationAccepted",
              "PresentationRejected",
              "ProblemReportPending",
              "ProblemReportSent",
              "ProblemReportReceived",
              "InvitationGenerated",
              "InvitationReceived"
            )
          )
        )
    object proofs
        extends Annotation[Seq[ProofRequestAux]](
          description =
            "The type of proofs requested in the context of this proof presentation request (e.g., VC schema, trusted issuers, etc.)",
          example = Seq.empty
        )
    object data
        extends Annotation[Seq[String]](
          description = "The list of proofs presented by the prover to the verifier.",
          example = Seq.empty
        )
    object connectionId
        extends Annotation[String](
          description = "The unique identifier of an established connection between the verifier and the prover.",
          example = "bc528dc8-69f1-4c5a-a508-5f8019047900"
        )

    object metaRetries
        extends Annotation[Int](
          description = "The maximum background processing attempts remaining for this record",
          example = 5
        )

    object metaLastFailure
        extends Annotation[ErrorResponse](
          description = "The last failure if any.",
          example =
            ErrorResponse.failureToErrorResponseConversion(FailureInfo("Error", StatusCode.NotFound, "Not Found"))
        )

    object goalcode
        extends Annotation[String](
          description =
            "A self-attested code the receiver may want to display to the user or use in automatically deciding what to do with the out-of-band message.",
          example = "present-vp"
        )

    object goal
        extends Annotation[String](
          description =
            "A self-attested string that the receiver may want to display to the user about the context-specific goal of the out-of-band message.",
          example = "To verify a Peter College Graduate credential"
        )

    object myDid
        extends Annotation[String](
          description = "The DID representing me as the inviter or invitee in this specific connection.",
          example = "did:peer:12345"
        )

    object invitation
        extends Annotation[OOBPresentationInvitation](
          description = "The invitation for this Request Presentation",
          example = OOBPresentationInvitation.Example
        )
  }

  given encoder: JsonEncoder[PresentationStatus] =
    DeriveJsonEncoder.gen[PresentationStatus]

  given decoder: JsonDecoder[PresentationStatus] =
    DeriveJsonDecoder.gen[PresentationStatus]

  given schema: Schema[PresentationStatus] = Schema.derived
}
