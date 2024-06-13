package org.hyperledger.identus.oid4vci.domain

import com.nimbusds.jose.*
import org.hyperledger.identus.agent.walletapi.memory.GenericSecretStorageInMemory
import org.hyperledger.identus.agent.walletapi.service.{ManagedDIDService, MockManagedDIDService}
import org.hyperledger.identus.agent.walletapi.storage.{DIDNonSecretStorage, MockDIDNonSecretStorage}
import org.hyperledger.identus.castor.core.model.did.{DID, PrismDID, VerificationRelationship}
import org.hyperledger.identus.castor.core.service.{DIDService, MockDIDService}
import org.hyperledger.identus.oid4vci.http.{ClaimDescriptor, CredentialDefinition, Localization}
import org.hyperledger.identus.oid4vci.service.{OIDCCredentialIssuerService, OIDCCredentialIssuerServiceImpl}
import org.hyperledger.identus.oid4vci.storage.InMemoryIssuanceSessionService
import org.hyperledger.identus.pollux.core.repository.{
  CredentialRepository,
  CredentialRepositoryInMemory,
  CredentialStatusListRepositoryInMemory
}
import org.hyperledger.identus.pollux.core.service.*
import org.hyperledger.identus.pollux.vc.jwt.PrismDidResolver
import org.hyperledger.identus.shared.models.WalletId
import zio.{Clock, Random, URLayer, ZIO, ZLayer}
import zio.json.*
import zio.json.ast.Json
import zio.mock.MockSpecDefault
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID
import scala.util.Try

object OIDCCredentialIssuerServiceSpec
    extends MockSpecDefault
    with CredentialServiceSpecHelper
    with Openid4VCIProofJwtOps {

  val layers: URLayer[
    DIDService & ManagedDIDService & DIDNonSecretStorage,
    CredentialService & CredentialDefinitionService & OIDCCredentialIssuerService
  ] =
    ZLayer.makeSome[
      DIDService & ManagedDIDService & DIDNonSecretStorage,
      CredentialService & CredentialDefinitionService & OIDCCredentialIssuerService
    ](
      InMemoryIssuanceSessionService.layer,
      CredentialRepositoryInMemory.layer,
      CredentialStatusListRepositoryInMemory.layer,
      PrismDidResolver.layer,
      ResourceURIDereferencerImpl.layer,
      credentialDefinitionServiceLayer,
      GenericSecretStorageInMemory.layer,
      LinkSecretServiceImpl.layer,
      CredentialServiceImpl.layer,
      OIDCCredentialIssuerServiceImpl.layer
    )

  override def spec = suite("CredentialServiceImpl")(
    OIDCCredentialIssuerServiceSpec,
    validateProofSpec
  )

  private val (_, issuerKp, issuerDidMetadata, issuerDidData) =
    MockDIDService.createDID(VerificationRelationship.AssertionMethod)

  private val (holderOp, holderKp, holderDidMetadata, holderDidData) =
    MockDIDService.createDID(VerificationRelationship.AssertionMethod)

  private val holderDidServiceExpectations =
    MockDIDService.resolveDIDExpectation(holderDidMetadata, holderDidData)

  private val issuerDidServiceExpectations =
    MockDIDService.resolveDIDExpectation(issuerDidMetadata, issuerDidData)

  private val issuerManagedDIDServiceExpectations =
    MockManagedDIDService.javaKeyPairWithDIDExpectation(issuerKp)

  private val getIssuerPrismDidWalletIdExpectation =
    MockDIDNonSecretStorage.getPrismDidWalletIdExpectation(issuerDidData.id, WalletId.default)

  private def buildJwtProof(nonce: String, aud: UUID, iat: Int) = {
    val longFormDid = PrismDID.buildLongFormFromOperation(holderOp)
    makeJwtProof(longFormDid, nonce, aud, iat, holderKp.privateKey)
  }

  private val validateProofSpec = suite("Validate holder's proof of possession using the LongFormPrismDID")(
    test("should validate the holder's proof of possession using the LongFormPrismDID") {
      for {
        credentialIssuer <- ZIO.service[OIDCCredentialIssuerService]
        nonce <- Random.nextString(10)
        aud <- Random.nextUUID
        iat <- Clock.instant.map(_.getEpochSecond.toInt)
        jwt = buildJwtProof(nonce, aud, iat)
        result <- credentialIssuer.verifyJwtProof(jwt)
      } yield assert(result)(equalTo(true))
    }.provideSomeLayer(
      holderDidServiceExpectations.toLayer ++
        MockManagedDIDService.empty ++
        MockDIDNonSecretStorage.empty >+> layers
    )
  )

  private val OIDCCredentialIssuerServiceSpec =
    suite("Simple JWT credential issuance")(
      test("should issue a JWT credential") {
        for {
          oidcCredentialIssuerService <- ZIO.service[OIDCCredentialIssuerService]
          credentialDefinition = CredentialDefinition(
            `@context` = Some(Seq("https://www.w3.org/2018/credentials/v1")),
            `type` = Seq("VerifiableCredential", "CertificateOfBirth"),
            credentialSubject = Some(
              Map(
                "name" ->
                  ClaimDescriptor(mandatory = Some(true), valueType = Some("string"), display = Seq.empty[Localization])
              )
            )
          )
          subjectDID <- ZIO.fromEither(DID.fromString("did:work:MDP8AsFhHzhwUvGNuYkX7T"))
          jwt <- oidcCredentialIssuerService
            .issueJwtCredential(
              issuerDidData.id,
              Some(subjectDID),
              Json("name" -> Json.Str("Alice")),
              None,
              credentialDefinition
            )
          _ <- zio.Console.printLine(jwt)
          jwtObject <- ZIO.fromTry(Try(JWSObject.parse(jwt.value)))
          payload <- ZIO.fromEither(Json.decoder.decodeJson(jwtObject.getPayload.toString).flatMap(_.as[Json.Obj]))
          vc <- ZIO.fromEither(payload.get("vc").get.as[Json.Obj])
          credentialSubject <- ZIO.fromEither(vc.get("credentialSubject").get.as[Json.Obj])
          name <- ZIO.fromEither(credentialSubject.get("name").get.as[String])
        } yield assert(jwt.value)(isNonEmptyString) &&
          // assert(jwtObject.getHeader.getKeyID)(equalTo(issuerDidData.id.toString)) && //TODO: add key ID to the header
          assert(jwtObject.getHeader.getAlgorithm)(equalTo(JWSAlgorithm.ES256K)) &&
          assert(name)(equalTo("Alice"))
      }.provideSomeLayer(
        issuerDidServiceExpectations.toLayer ++
          issuerManagedDIDServiceExpectations.toLayer ++
          getIssuerPrismDidWalletIdExpectation.toLayer >+> layers
      )
    )
}
