package io.iohk.atala.pollux.core.service

import io.circe.Json
import io.iohk.atala.castor.core.model.did.CanonicalPrismDID
import io.iohk.atala.event.notification.{Event, EventNotificationService}
import io.iohk.atala.iris.proto.service.IrisServiceGrpc.IrisServiceStub
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.issuecredential.{IssueCredential, OfferCredential, RequestCredential}
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.model.{DidCommID, IssueCredentialRecord}
import io.iohk.atala.pollux.core.repository.CredentialRepository
import io.iohk.atala.pollux.vc.jwt.{DidResolver, JWT}
import zio.{IO, Task, ULayer, URLayer, ZIO, ZLayer}

class CredentialServiceWithEventNotificationImpl(
    irisClient: IrisServiceStub,
    credentialRepository: CredentialRepository[Task],
    didResolver: DidResolver,
    uriDereferencer: URIDereferencer,
    eventNotificationService: EventNotificationService
) extends CredentialServiceImpl(irisClient, credentialRepository, didResolver, uriDereferencer) {

  override def createIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      maybeSchemaId: Option[_root_.java.lang.String],
      claims: Json,
      validityPeriod: Option[Double],
      automaticIssuance: Option[Boolean],
      awaitConfirmation: Option[Boolean],
      issuingDID: Option[CanonicalPrismDID]
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(
      super.createIssueCredentialRecord(
        pairwiseIssuerDID,
        pairwiseHolderDID,
        thid,
        maybeSchemaId,
        claims,
        validityPeriod,
        automaticIssuance,
        awaitConfirmation,
        issuingDID
      )
    )

  override def markOfferSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.markOfferSent(recordId))

  override def receiveCredentialOffer(offer: OfferCredential): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.receiveCredentialOffer(offer))

  override def acceptCredentialOffer(
      recordId: DidCommID,
      subjectId: String
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.acceptCredentialOffer(recordId, subjectId))

  override def generateCredentialRequest(
      recordId: DidCommID,
      signedPresentation: JWT
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.generateCredentialRequest(recordId, signedPresentation))

  override def markRequestSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.markRequestSent(recordId))

  override def receiveCredentialRequest(request: RequestCredential): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.receiveCredentialRequest(request))

  override def acceptCredentialRequest(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.acceptCredentialRequest(recordId))

  override def markCredentialGenerated(
      recordId: DidCommID,
      issueCredential: IssueCredential
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.markCredentialGenerated(recordId, issueCredential))

  override def markCredentialSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.markCredentialSent(recordId))

  override def receiveCredentialIssue(issue: IssueCredential): IO[CredentialServiceError, IssueCredentialRecord] =
    notify(super.receiveCredentialIssue(issue))

  // Notification method
  private[this] def notify(effect: IO[CredentialServiceError, IssueCredentialRecord]) = for {
    record <- effect
    _ <- eventNotificationService
      .notify(Event(s"${record.protocolState.toString} [${record.id}]"))
      .catchAll(e => ZIO.logError(s"Notification service error: $e"))
  } yield record
}

object CredentialServiceWithEventNotificationImpl {
  val layer: URLayer[
    IrisServiceStub with CredentialRepository[Task] with DidResolver with URIDereferencer with EventNotificationService,
    CredentialServiceWithEventNotificationImpl
  ] = ZLayer.fromFunction(CredentialServiceWithEventNotificationImpl(_, _, _, _, _))
}
