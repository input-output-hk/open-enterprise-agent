package io.iohk.atala.iam.oidc

import io.iohk.atala.agent.walletapi.model.BaseEntity
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.iam.authentication.{Authenticator, DefaultAuthenticator, SecurityLogic}
import io.iohk.atala.iam.oidc.http.{
  CredentialErrorResponse,
  CredentialRequest,
  ImmediateCredentialResponse,
  NonceResponse
}
import sttp.tapir.ztapir.*
import zio.*

import java.time.Instant
import java.util.UUID

class CredentialIssuerServerEndpoints(
    authenticator: Authenticator[BaseEntity]
) {
  val credentialServerEndpoint: ZServerEndpoint[Any, Any] =
    CredentialIssuerEndpoints.credentialEndpoint
      .zServerSecurityLogic(
        SecurityLogic // TODO: add OIDC client authenticator
          .authenticate(_)(authenticator)
          .mapError(Left[ErrorResponse, CredentialErrorResponse])
      )
      .serverLogic { wac =>
        { case (ctx: RequestContext, didRef: String, request: CredentialRequest) =>
          ZIO.succeed(ImmediateCredentialResponse("credential"))
        }
      }

  val nonceServerEndpoint: ZServerEndpoint[Any, Any] =
    CredentialIssuerEndpoints.nonceEndpoint
      .zServerSecurityLogic(
        SecurityLogic // TODO: add OIDC client authenticator
          .authenticate(_)(authenticator)
          .mapError(Left[ErrorResponse, CredentialErrorResponse])
      )
      .serverLogic { wac =>
        { case (ctx: RequestContext, didRef: String) =>
          ZIO.succeed(NonceResponse(UUID.randomUUID().toString, Instant.now().plusSeconds(60).toEpochMilli()))
        }
      }

  val all: List[ZServerEndpoint[Any, Any]] = List(credentialServerEndpoint, nonceServerEndpoint)
}

object CredentialIssuerServerEndpoints {
  def all: URIO[DefaultAuthenticator, List[ZServerEndpoint[Any, Any]]] = {
    for {
      authenticator <- ZIO.service[DefaultAuthenticator]
      oidcEndpoints = new CredentialIssuerServerEndpoints(authenticator)
    } yield oidcEndpoints.all
  }
}
