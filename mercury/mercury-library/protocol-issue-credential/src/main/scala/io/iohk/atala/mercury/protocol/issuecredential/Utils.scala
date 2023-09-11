package io.iohk.atala.mercury.protocol.issuecredential

import io.circe.syntax._
import io.circe.parser._

import io.iohk.atala.mercury.model._
import io.circe.Decoder

// private[this] trait BodyUtils {
//   def formats: Seq[CredentialFormat]
// }

private[this] trait ReadAttachmentsUtils {

  // def body: BodyUtils
  def attachments: Seq[AttachmentDescriptor]

  // TODO this formatName shoud be type safe
  lazy val getCredentialFormatAndCredential: Seq[(String, String, Array[Byte])] =
    attachments
      .flatMap(attachment =>
        attachment.format.map { formatName =>
          attachment.data match {
            case obj: JwsData  => ??? // TODO
            case obj: Base64   => (attachment.id, formatName, obj.base64.getBytes())
            case obj: LinkData => ??? // TODO Does this make sens
            case obj: JsonData =>
              (
                attachment.id,
                formatName,
                java.util.Base64
                  .getUrlEncoder()
                  .encode(obj.json.asJson.noSpaces.getBytes())
              )
          }
        }
      )

  /** @return
    *   credential data (of a certain format type) in an array of Bytes encoded in base 64
    */
  def getCredential[A](credentialFormatName: String)(using decodeA: Decoder[A]): Seq[A] =
    getCredentialFormatAndCredential
      .filter(_._2 == credentialFormatName)
      .map(e => java.util.Base64.getUrlDecoder().decode(e._3))
      .map(String(_))
      .map(e => decode[A](e))
      .flatMap(_.toOption)

}
