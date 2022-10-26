package io.iohk.atala.mercury.protocol.issuecredential

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import io.iohk.atala.mercury.model.{AttachmentData, PIURI}

/** ALL parameterS are DIDCOMMV2 format and naming conventions and follows the protocol
  * @see
  *   https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2
  *
  * @param id
  * @param `type`
  * @param body
  * @param attachments
  */
final case class IssueCredential(id: String, `type`: PIURI, body: IssueCredential.Body, attachments: AttachmentData) {
  assert(`type` == IssueCredential.`type`)
}

object IssueCredential {

  def `type`: PIURI = "https://didcomm.org/issue-credential/2.0/issue-credential"

  final case class Body(
      goal_code: Option[String],
      comment: Option[String],
      replacement_id: Option[String],
      multiple_available: Option[String],
      credential_preview: Option[CredentialPreview],
      formats: Seq[CredentialFormat]
  )
}
