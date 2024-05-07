package org.hyperledger.identus.pollux.sql.repository

import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import org.hyperledger.identus.pollux.core.model.oidc4vc.CredentialConfiguration
import org.hyperledger.identus.pollux.core.model.oidc4vc.CredentialIssuer
import org.hyperledger.identus.pollux.core.repository.OIDC4VCIssuerMetadataRepository
import org.hyperledger.identus.shared.db.ContextAwareTask
import org.hyperledger.identus.shared.db.Implicits.*
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.*
import zio.interop.catz.*

import java.net.URL
import java.time.Instant
import java.util.UUID

// TODO: implement all members
class JdbcOIDC4VCIssuerMetadataRepository(xa: Transactor[ContextAwareTask], xb: Transactor[Task])
    extends OIDC4VCIssuerMetadataRepository {

  override def findIssuerById(issuerId: UUID): UIO[Option[CredentialIssuer]] = {
    val cxnIO = sql"""
      |SELECT
      |  id,
      |  authorization_server,
      |  created_at,
      |  updated_at
      |FROM public.issuer_metadata
      |WHERE id = $issuerId
      """.stripMargin
      .query[CredentialIssuer]
      .option

    cxnIO
      .transact(xb)
      .orDie
  }

  override def findWalletIssuers: URIO[WalletAccessContext, Seq[CredentialIssuer]] = {
    val cxnIO = sql"""
      |SELECT
      |  id,
      |  authorization_server,
      |  created_at,
      |  updated_at
      |FROM public.issuer_metadata
      """.stripMargin
      .query[CredentialIssuer]
      .to[Seq]

    cxnIO
      .transactWallet(xa)
      .orDie
  }

  override def createIssuer(issuer: CredentialIssuer): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        |INSERT INTO public.issuer_metadata (
        |  id,
        |  authorization_server,
        |  created_at,
        |  updated_at,
        |  wallet_id
        |) VALUES (
        |  ${issuer.id},
        |  ${issuer.authorizationServer},
        |  ${issuer.createdAt},
        |  ${issuer.updatedAt},
        |  current_setting('app.current_wallet_id')::UUID
        |)
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def updateIssuer(
      issuerId: UUID,
      authorizationServer: Option[URL]
  ): URIO[WalletAccessContext, Unit] = {
    val setFr = (now: Instant) =>
      Fragments.setOpt(
        Some(fr"updated_at = $now"),
        authorizationServer.map(url => fr"authorization_server = $url")
      )
    val cxnIO = (setFr: Fragment) => sql"""
        |UPDATE public.issuer_metadata
        |$setFr
        |WHERE id = $issuerId
        """.stripMargin.update

    for {
      now <- ZIO.clockWith(_.instant)
      _ <- cxnIO(setFr(now)).run
        .transactWallet(xa)
        .ensureOneAffectedRowOrDie
    } yield ()
  }

  override def deleteIssuer(issuerId: UUID): URIO[WalletAccessContext, Unit] = {
    val cxnIO = sql"""
        | DELETE FROM public.issuer_metadata
        | WHERE id = $issuerId
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def findAllCredentialConfigurations(issuerId: UUID): UIO[Seq[CredentialConfiguration]] = ???

  override def createCredentialConfiguration(
      issuerId: UUID,
      config: CredentialConfiguration
  ): URIO[WalletAccessContext, Unit] = ???

  override def deleteCredentialConfiguration(
      issuerId: UUID,
      configurationId: String
  ): URIO[WalletAccessContext, Unit] = ???

}

object JdbcOIDC4VCIssuerMetadataRepository {
  val layer: URLayer[Transactor[ContextAwareTask] & Transactor[Task], OIDC4VCIssuerMetadataRepository] =
    ZLayer.fromFunction(new JdbcOIDC4VCIssuerMetadataRepository(_, _))
}
