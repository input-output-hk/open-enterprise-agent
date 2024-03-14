package io.iohk.atala.connect.core.service

import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.connect.core.model.error.ConnectionServiceError
import io.iohk.atala.event.notification.{Event, EventNotificationService}
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import io.iohk.atala.shared.models.WalletAccessContext
import zio.{URLayer, ZIO, ZLayer, URIO, UIO}
import java.time.Duration
import java.util.UUID
import io.iohk.atala.connect.core.model.error.ConnectionServiceError.InvitationParsingError
import io.iohk.atala.connect.core.model.error.ConnectionServiceError.InvitationAlreadyReceived
import io.iohk.atala.connect.core.model.error.ConnectionServiceError.InvalidStateForOperation
import io.iohk.atala.connect.core.model.error.ConnectionServiceError.RecordIdNotFound

class ConnectionServiceNotifier(
    svc: ConnectionService,
    eventNotificationService: EventNotificationService
) extends ConnectionService {

  private val connectionUpdatedEvent = "ConnectionUpdated"

  override def createConnectionInvitation(
      label: Option[String],
      goalCode: Option[String],
      goal: Option[String],
      pairwiseDID: DidId
  ): URIO[WalletAccessContext, ConnectionRecord] =
    notifyOnSuccess(svc.createConnectionInvitation(label, goalCode, goal, pairwiseDID))

  override def receiveConnectionInvitation(
      invitation: String
  ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord] =
    notifyOnSuccess(svc.receiveConnectionInvitation(invitation))

  override def acceptConnectionInvitation(
      recordId: UUID,
      pairwiseDid: DidId
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.acceptConnectionInvitation(recordId, pairwiseDid))

  override def markConnectionRequestSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionRequestSent(recordId))

  override def receiveConnectionRequest(
      request: ConnectionRequest,
      expirationTime: Option[Duration]
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    notifyOnSuccess(svc.receiveConnectionRequest(request, expirationTime))

  override def acceptConnectionRequest(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    notifyOnSuccess(svc.acceptConnectionRequest(recordId))

  override def markConnectionResponseSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionResponseSent(recordId))

  override def markConnectionInvitationExpired(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionInvitationExpired(recordId))

  override def receiveConnectionResponse(
      response: ConnectionResponse
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    notifyOnSuccess(svc.receiveConnectionResponse(response))

  private[this] def notifyOnSuccess[E](effect: ZIO[WalletAccessContext, E, ConnectionRecord]) =
    for {
      record <- effect
      _ <- notify(record)
    } yield record

  private[this] def notify(record: ConnectionRecord) = {
    val result = for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      producer <- eventNotificationService.producer[ConnectionRecord]("Connect")
      _ <- producer.send(Event(connectionUpdatedEvent, record, walletId))
    } yield ()
    result.catchAll(e => ZIO.logError(s"Notification service error: $e"))
  }

  override def getConnectionRecord(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    svc.getConnectionRecord(recordId)

  override def getConnectionRecordByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    svc.getConnectionRecordByThreadId(thid)

  override def deleteConnectionRecord(recordId: UUID): URIO[WalletAccessContext, Unit] =
    svc.deleteConnectionRecord(recordId)

  override def reportProcessingFailure(
      recordId: UUID,
      failReason: Option[String]
  ): URIO[WalletAccessContext, Unit] =
    svc.reportProcessingFailure(recordId, failReason)

  override def getConnectionRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    svc.getConnectionRecords()

  override def getConnectionRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    svc.getConnectionRecordsByStates(ignoreWithZeroRetries, limit, states: _*)

  override def getConnectionRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): UIO[Seq[ConnectionRecord]] =
    svc.getConnectionRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, states: _*)
}

object ConnectionServiceNotifier {
  val layer: URLayer[ConnectionService & EventNotificationService, ConnectionService] =
    ZLayer.fromFunction(ConnectionServiceNotifier(_, _))
}
