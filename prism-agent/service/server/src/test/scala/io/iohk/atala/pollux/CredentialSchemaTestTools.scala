package io.iohk.atala.pollux

import io.iohk.atala.agent.walletapi.model.WalletSeed
import io.iohk.atala.agent.walletapi.model.{ManagedDIDState, PublicationState}
import io.iohk.atala.agent.walletapi.service.WalletManagementService
import io.iohk.atala.agent.walletapi.service.{ManagedDIDService, MockManagedDIDService}
import io.iohk.atala.api.http.ErrorResponse
import io.iohk.atala.castor.core.model.did.PrismDIDOperation
import io.iohk.atala.container.util.PostgresLayer.*
import io.iohk.atala.pollux.core.model.schema.`type`.CredentialJsonSchemaType
import io.iohk.atala.pollux.core.repository.CredentialSchemaRepository
import io.iohk.atala.pollux.core.service.CredentialSchemaServiceImpl
import io.iohk.atala.pollux.credentialschema.SchemaRegistryServerEndpoints
import io.iohk.atala.pollux.credentialschema.controller.{CredentialSchemaController, CredentialSchemaControllerImpl}
import io.iohk.atala.pollux.credentialschema.http.{
  CredentialSchemaInput,
  CredentialSchemaResponse,
  CredentialSchemaResponsePage
}
import io.iohk.atala.pollux.sql.repository.JdbcCredentialSchemaRepository
import sttp.client3.testing.SttpBackendStub
import sttp.client3.ziojson.*
import sttp.client3.{DeserializationException, Response, UriContext, basicRequest}
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.CustomiseInterceptors
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.ast.Json
import zio.json.ast.Json.*
import zio.json.{DecoderOps, EncoderOps}
import zio.mock.Expectation
import zio.test.{Assertion, Gen, ZIOSpecDefault}

import java.time.OffsetDateTime
import io.iohk.atala.shared.models.WalletAccessContext

trait CredentialSchemaTestTools {
  self: ZIOSpecDefault =>

  type SchemaBadRequestResponse =
    Response[Either[DeserializationException[String], ErrorResponse]]
  type SchemaResponse =
    Response[Either[DeserializationException[String], CredentialSchemaResponse]]
  type SchemaPageResponse =
    Response[
      Either[DeserializationException[String], CredentialSchemaResponsePage]
    ]

  private val pgLayer = postgresLayer(verbose = false)
  private val transactorLayer = pgLayer >>> hikariConfigLayer >>> transactor
  private val controllerLayer = transactorLayer >>>
    JdbcCredentialSchemaRepository.layer >+>
    CredentialSchemaServiceImpl.layer >+>
    CredentialSchemaControllerImpl.layer

  val mockManagedDIDServiceLayer: Expectation[ManagedDIDService] = MockManagedDIDService
    .GetManagedDIDState(
      assertion = Assertion.anything,
      result = Expectation.value(
        Some(
          ManagedDIDState(
            PrismDIDOperation.Create(Nil, Nil, Nil),
            0,
            PublicationState.Published(scala.collection.immutable.ArraySeq.empty)
          )
        )
      )
    )

  val testEnvironmentLayer = zio.test.testEnvironment ++
    pgLayer ++
    transactorLayer ++
    controllerLayer

  val credentialSchemaUriBase = uri"http://test.com/schema-registry/schemas"

  def bootstrapOptions[F[_]](monadError: MonadError[F]) = {
    new CustomiseInterceptors[F, Any](_ => ())
      .defaultHandlers(ErrorResponse.failureResponseHandler)
  }

  def httpBackend(controller: CredentialSchemaController, wallet: WalletAccessContext) = {
    val schemaRegistryEndpoints = SchemaRegistryServerEndpoints(controller, wallet)

    val backend =
      TapirStubInterpreter(
        bootstrapOptions(new RIOMonadError[Any]),
        SttpBackendStub(new RIOMonadError[Any])
      )
        .whenServerEndpoint(schemaRegistryEndpoints.createSchemaServerEndpoint)
        .thenRunLogic()
        .whenServerEndpoint(schemaRegistryEndpoints.getSchemaByIdServerEndpoint)
        .thenRunLogic()
        .whenServerEndpoint(
          schemaRegistryEndpoints.lookupSchemasByQueryServerEndpoint
        )
        .thenRunLogic()
        .backend()
    backend
  }

  def deleteAllCredentialSchemas: RIO[CredentialSchemaRepository[Task], Long] = {
    for {
      repository <- ZIO.service[CredentialSchemaRepository[Task]]
      count <- repository.deleteAll()
    } yield count
  }
}

trait CredentialSchemaGen {
  self: ZIOSpecDefault with CredentialSchemaTestTools =>
  object Generator {
    val schemaName = Gen.alphaNumericStringBounded(4, 12)
    val majorVersion = Gen.int(1, 9)
    val minorVersion = Gen.int(0, 9)
    val patchVersion = Gen.int(0, 9)
    val schemaVersion = majorVersion <*> minorVersion <*> patchVersion map (v => s"${v._1}.${v._2}.${v._3}")
    val schemaDescription = Gen.alphaNumericStringBounded(5, 30)
    val schemaAttribute = Gen.alphaNumericStringBounded(3, 9)
    val schemaAttributes = Gen.setOfBounded(1, 4)(schemaAttribute).map(_.toList)
    val schemaAuthored = Gen.offsetDateTime(
      min = OffsetDateTime.now().minusMonths(6),
      max = OffsetDateTime.now()
    )
    val schemaTag: Gen[Any, String] = Gen.alphaNumericStringBounded(3, 5)
    val schemaTags: Gen[Any, List[String]] =
      Gen.setOfBounded(0, 3)(schemaTag).map(_.toList)

    val schemaAuthor =
      Gen.int(1000000, 9999999).map(i => s"did:prism:4fb06243213500578f59588de3e1dd9b266ec1b61e43b0ff86ad0712f$i")

    val jsonSchema =
      """
        |{
        |    "$schema": "https://json-schema.org/draft/2020-12/schema",
        |    "description": "Driving License",
        |    "type": "object",
        |    "properties": {
        |        "name" : "Alice"
        |    },
        |    "required": [
        |        "name"
        |    ]
        |}
        |""".stripMargin

    val schemaInput = for {
      name <- schemaName
      version <- schemaVersion
      description <- schemaDescription
      author <- schemaAuthor
      tags <- schemaTags
    } yield CredentialSchemaInput(
      name = name,
      version = version,
      description = Some(description),
      `type` = CredentialJsonSchemaType.`type`,
      schema = jsonSchema.fromJson[Json].getOrElse(Json.Null),
      tags = tags,
      author = author
    )
  }

  def generateSchemasN(
      count: Int
  ): ZIO[CredentialSchemaController, Throwable, List[CredentialSchemaInput]] =
    for {
      controller <- ZIO.service[CredentialSchemaController]
      walletId <- ZIO.serviceWithZIO[WalletManagementService](_.createWallet(WalletSeed.fromByteArray(Array.empty)))
      walletAccessCtx = WalletAccessContext(walletId)
      backend = httpBackend(controller, walletAccessCtx)
      inputs <- Generator.schemaInput.runCollectN(count)
      _ <- inputs
        .map(in =>
          basicRequest
            .post(credentialSchemaUriBase)
            .body(in.toJsonPretty)
            .response(asJsonAlways[CredentialSchemaResponse])
            .send(backend)
        )
        .reduce((l, r) => l.flatMap(_ => r))
    } yield inputs
}
