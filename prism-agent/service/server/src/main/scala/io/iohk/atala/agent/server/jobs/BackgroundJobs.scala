package io.iohk.atala.agent.server.jobs

import scala.jdk.CollectionConverters.*
import zio.*
import io.iohk.atala.pollux.core.service.CredentialService
import io.iohk.atala.pollux.core.model.IssueCredentialRecord
import io.iohk.atala.pollux.core.model.error.CreateCredentialPayloadFromRecordError
import io.iohk.atala.pollux.core.model.error.IssueCredentialError
import io.iohk.atala.pollux.core.model.error.MarkCredentialRecordsAsPublishQueuedError
import io.iohk.atala.pollux.core.model.error.PublishCredentialBatchError
import io.iohk.atala.pollux.core.service.CredentialService
import io.iohk.atala.pollux.vc.jwt.W3cCredentialPayload
import zio.*

import java.time.Instant

import io.iohk.atala.mercury.DidComm
import io.iohk.atala.mercury.MediaTypes
import io.iohk.atala.mercury.model._
import io.iohk.atala.mercury.model.error._
import io.iohk.atala.mercury.protocol.issuecredential._
import io.iohk.atala.resolvers.UniversalDidResolver
import io.iohk.atala.agent.server.jobs.MercuryUtils.sendMessage
import java.io.IOException

import zhttp.service._
import zhttp.http._

object BackgroundJobs {

  val didCommExchanges = {
    for {
      credentialService <- ZIO.service[CredentialService]
      records <- credentialService
        .getIssueCredentialRecords()
        .mapError(err => Throwable(s"Error occured while getting issue credential records: $err"))
      _ <- ZIO.foreach(records)(performExchange)
    } yield ()
  }

  private[this] def performExchange(
      record: IssueCredentialRecord
  ): ZIO[DidComm & CredentialService, Throwable, Unit] = {
    import IssueCredentialRecord._
    import IssueCredentialRecord.ProtocolState._
    import IssueCredentialRecord.PublicationState._
    val aux = for {
      _ <- ZIO.logDebug(s"Running action with records => $record")
      _ <- record match {
        // Offer should be sent from Issuer to Holder
        case IssueCredentialRecord(id, _, _, _, _, Role.Issuer, _, _, _, _, OfferPending, _, Some(offer), _, _) =>
          for {
            _ <- ZIO.log(s"IssueCredentialRecord: OfferPending (START)")
            didComm <- ZIO.service[DidComm]
            // offer = OfferCredential.build( // TODO
            //   // body = body FIXME
            //   // attachments = Seq(attachmentDescriptor),
            //   to = offer2.fromDID,
            //   from = didComm.myDid,
            //   thid = Some(offer.thid),
            //   credential_preview = ??? // FXOME
            // )
            // msg = offer.makeMessage
            // _ <- sendMessage(msg)
            _ <- sendMessage(offer.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markOfferSent(id)
          } yield ()

        // Request should be sent from Holder to Issuer
        case IssueCredentialRecord(id, _, _, _, _, Role.Holder, _, _, _, _, RequestPending, _, _, Some(request), _) =>
          for {
            _ <- sendMessage(request.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markRequestSent(id)
          } yield ()

        // 'automaticIssuance' is TRUE. Issuer automatically accepts the Request
        case IssueCredentialRecord(id, _, _, _, _, Role.Issuer, _, _, Some(true), _, RequestReceived, _, _, _, _) =>
          for {
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.acceptCredentialRequest(id)
          } yield ()

        // Credential is pending, can be generated by Issuer and optionally published on-chain
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(awaitConfirmation),
              CredentialPending,
              _,
              _,
              _,
              Some(issue)
            ) =>
          for {
            credentialService <- ZIO.service[CredentialService]
            w3Credential <- credentialService.createCredentialPayloadFromRecord(
              record,
              credentialService.createIssuer,
              Instant.now()
            )
          } yield ()
          // Generate the JWT Credential and store it in DB as an attacment to IssueCredentialData
          // Set ProtocolState to CredentialGenerated
          // Set PublicationState to PublicationPending
          for {
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markCredentialGenerated(id)
            _ <- if (awaitConfirmation) credentialService.markCredentialPublicationPending(id) else ZIO.succeed(())
          } yield ()

        // Credential has been generated and can be sent directly to the Holder
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(false),
              CredentialGenerated,
              None,
              _,
              _,
              Some(issue)
            ) =>
          for {
            _ <- sendMessage(issue.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markCredentialSent(id)
          } yield ()

        // Credential has been generated, published, and can now be sent to the Holder
        case IssueCredentialRecord(
              id,
              _,
              _,
              _,
              _,
              Role.Issuer,
              _,
              _,
              _,
              Some(true),
              CredentialGenerated,
              Some(Published),
              _,
              _,
              Some(issue)
            ) =>
          for {
            _ <- sendMessage(issue.makeMessage)
            credentialService <- ZIO.service[CredentialService]
            _ <- credentialService.markCredentialSent(id)
          } yield ()

        case IssueCredentialRecord(id, _, _, _, _, _, _, _, _, _, ProblemReportPending, _, _, _, _) => ???
        case IssueCredentialRecord(id, _, _, _, _, _, _, _, _, _, _, _, _, _, _)                    => ZIO.unit
      }
    } yield ()

    aux.catchAll {
      case ex: TransportError => // : io.iohk.atala.mercury.model.error.MercuryError | java.io.IOException =>
        ex.printStackTrace()
        ZIO.logError(ex.getMessage()) *>
          ZIO.fail(mercuryErrorAsThrowable(ex))
      case ex: IOException => ZIO.fail(ex)
    }
  }

  val publishCredentialsToDlt = {
    for {
      credentialService <- ZIO.service[CredentialService]
      _ <- performPublishCredentialsToDlt(credentialService)
    } yield ()

  }

  private[this] def performPublishCredentialsToDlt(credentialService: CredentialService) = {
    type PublishToDltError = IssueCredentialError | CreateCredentialPayloadFromRecordError |
      PublishCredentialBatchError | MarkCredentialRecordsAsPublishQueuedError

    val res: ZIO[Any, PublishToDltError, Unit] = for {
      records <- credentialService.getCredentialRecordsByState(IssueCredentialRecord.ProtocolState.CredentialPending)
      // NOTE: the line below is a potentially slow operation, because <createCredentialPayloadFromRecord> makes a database SELECT call,
      // so calling this function n times will make n database SELECT calls, while it can be optimized to get
      // all data in one query, this function here has to be refactored as well. Consider doing this if this job is too slow
      credentials <- ZIO.foreach(records) { record =>
        credentialService.createCredentialPayloadFromRecord(record, credentialService.createIssuer, Instant.now())
      }
      // FIXME: issuer here should come from castor not from credential service, this needs to be done before going to prod
      publishedBatchData <- credentialService.publishCredentialBatch(credentials, credentialService.createIssuer)
      _ <- credentialService.markCredentialRecordsAsPublishQueued(publishedBatchData.credentialsAnsProofs)
      // publishedBatchData gives back irisOperationId, which should be persisted to track the status
    } yield ()

    ZIO.unit
  }

}
