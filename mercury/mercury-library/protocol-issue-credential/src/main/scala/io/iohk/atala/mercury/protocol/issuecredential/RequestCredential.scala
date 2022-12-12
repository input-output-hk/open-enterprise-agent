package io.iohk.atala.mercury.protocol.issuecredential

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import io.iohk.atala.mercury.model.PIURI
import io.iohk.atala.mercury.model._

final case class RequestCredential(
    id: String = java.util.UUID.randomUUID.toString(),
    `type`: PIURI = RequestCredential.`type`,
    body: RequestCredential.Body,
    attachments: Seq[AttachmentDescriptor],
    // extra
    thid: Option[String] = None,
    from: DidId,
    to: DidId,
) extends ReadAttachmentsUtils {

  def makeMessage: Message = Message(
    id = this.id,
    piuri = this.`type`,
    from = Some(this.from),
    to = Seq(this.to),
    thid = this.thid,
    body = this.body.asJson.asObject.get, // TODO get
    attachments = Some(this.attachments),
  )
}
object RequestCredential {

  import AttachmentDescriptor.attachmentDescriptorEncoderV2
  given Encoder[RequestCredential] = deriveEncoder[RequestCredential]
  given Decoder[RequestCredential] = deriveDecoder[RequestCredential]

  def `type`: PIURI = "https://didcomm.org/issue-credential/2.0/request-credential"

  def build[A](
      fromDID: DidId,
      toDID: DidId,
      thid: Option[String] = None,
      credentials: Map[String, A] = Map.empty,
  )(using Encoder[A]): RequestCredential = {
    val aux = credentials.map { case (formatName, singleCredential) =>
      val attachment = AttachmentDescriptor.buildAttachment(payload = singleCredential)
      val credentialFormat: CredentialFormat = CredentialFormat(attachment.id, formatName)
      (credentialFormat, attachment)
    }
    RequestCredential(
      thid = thid,
      from = fromDID,
      to = toDID,
      body = Body(formats = aux.keys.toSeq),
      attachments = aux.values.toSeq
    )
  }

  final case class Body(
      goal_code: Option[String] = None,
      comment: Option[String] = None,
      formats: Seq[CredentialFormat] = Seq.empty[CredentialFormat]
  ) extends BodyUtils

  object Body {
    given Encoder[Body] = deriveEncoder[Body]
    given Decoder[Body] = deriveDecoder[Body]
  }

  def makeRequestCredentialFromOffer(msg: Message): RequestCredential = { // TODO change msg: Message to RequestCredential
    val oc: OfferCredential = OfferCredential.readFromMessage(msg)

    RequestCredential(
      body = RequestCredential.Body(
        goal_code = oc.body.goal_code,
        comment = oc.body.comment,
        formats = oc.body.formats,
      ),
      attachments = oc.attachments,
      thid = msg.thid.orElse(Some(oc.id)),
      from = oc.to,
      to = oc.from,
    )
  }

  def readFromMessage(message: Message): RequestCredential =
    val body = message.body.asJson.as[RequestCredential.Body].toOption.get // TODO get

    RequestCredential(
      id = message.id,
      `type` = message.piuri,
      body = body,
      attachments = message.attachments.getOrElse(Seq.empty),
      thid = message.thid,
      from = message.from.get, // TODO get
      to = {
        assert(message.to.length == 1, "The recipient is ambiguous. Need to have only 1 recipient") // TODO return error
        message.to.head
      },
    )

}
