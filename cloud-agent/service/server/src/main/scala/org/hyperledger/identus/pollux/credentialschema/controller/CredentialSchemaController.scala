package org.hyperledger.identus.pollux.credentialschema.controller

import org.hyperledger.identus.api.http.*
import org.hyperledger.identus.api.http.model.{Order, Pagination}
import org.hyperledger.identus.pollux.credentialschema.http.{
  CredentialSchemaDidUrlResponse,
  CredentialSchemaDidUrlResponsePage,
  CredentialSchemaInnerDidUrlResponse,
  CredentialSchemaInput,
  CredentialSchemaResponse,
  CredentialSchemaResponsePage,
  FilterInput
}
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.*
import zio.json.ast.Json

import java.util.UUID
import scala.language.implicitConversions

trait CredentialSchemaController {
  def createSchema(in: CredentialSchemaInput)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaResponse]

  def createSchemaDidUrl(baseUrlServiceName: String, in: CredentialSchemaInput)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaDidUrlResponse]

  def updateSchema(id: UUID, in: CredentialSchemaInput)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaResponse]

  def updateSchemaDidUrl(baseUrlServiceName: String, id: UUID, in: CredentialSchemaInput)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaDidUrlResponse]

  def getSchemaByGuid(id: UUID)(implicit
      rc: RequestContext
  ): IO[ErrorResponse, CredentialSchemaResponse]

  def getSchemaByGuidDidUrl(baseUrlServiceName: String, id: UUID)(implicit
      rc: RequestContext
  ): IO[ErrorResponse, CredentialSchemaDidUrlResponse]

  def getSchemaJsonByGuid(id: UUID)(implicit
      rc: RequestContext
  ): IO[ErrorResponse, Json]

  def getSchemaJsonByGuidDidUrl(baseUrlServiceName: String, id: UUID)(implicit
      rc: RequestContext
  ): IO[ErrorResponse, CredentialSchemaInnerDidUrlResponse]

  def lookupSchemas(
      filter: FilterInput,
      pagination: Pagination,
      order: Option[Order],
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaResponsePage]

  def lookupSchemasDidUrl(
      baseUrlServiceName: String,
      filter: FilterInput,
      pagination: Pagination,
      order: Option[Order],
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialSchemaDidUrlResponsePage]
}
