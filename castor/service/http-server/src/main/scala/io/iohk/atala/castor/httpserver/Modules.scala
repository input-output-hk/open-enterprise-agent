package io.iohk.atala.castor.httpserver

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import io.iohk.atala.castor.core.service.{
  DIDAuthenticationService,
  DIDService,
  MockDIDAuthenticationService,
  MockDIDService
}
import io.iohk.atala.castor.httpserver.apimarshaller.{DIDApiMarshallerImpl, DIDAuthenticationApiMarshallerImpl}
import io.iohk.atala.castor.httpserver.apiservice.{DIDApiServiceImpl, DIDAuthenticationApiServiceImpl}
import io.iohk.atala.castor.openapi.api.{
  DIDApi,
  DIDAuthenticationApi,
  DIDAuthenticationApiMarshaller,
  DIDAuthenticationApiService
}
import zio.*

object Modules {

  val actorSystemLayer: TaskLayer[ActorSystem[Nothing]] = ZLayer.scoped(
    ZIO.acquireRelease(ZIO.attempt(ActorSystem(Behaviors.empty, "actor-system")))(system =>
      ZIO.attempt(system.terminate()).orDie
    )
  )

  val didApiLayer: ULayer[DIDApi] = {
    val serviceLayer = MockDIDService.layer
    val apiServiceLayer = serviceLayer >>> DIDApiServiceImpl.layer
    val apiMarshallerLayer = DIDApiMarshallerImpl.layer
    (apiServiceLayer ++ apiMarshallerLayer) >>> ZLayer.fromFunction(new DIDApi(_, _))
  }

  val didAuthenticationApiLayer: ULayer[DIDAuthenticationApi] = {
    val serviceLayer = MockDIDAuthenticationService.layer
    val apiServiceLayer = serviceLayer >>> DIDAuthenticationApiServiceImpl.layer
    val apiMarshallerLayer = DIDAuthenticationApiMarshallerImpl.layer
    (apiServiceLayer ++ apiMarshallerLayer) >>> ZLayer.fromFunction(new DIDAuthenticationApi(_, _))
  }

  val app = {
    val server = for {
      routes <- HttpRoutes.routes
      _ <- HttpServer.start(8000, routes)
    } yield ()

    server.provideLayer(actorSystemLayer ++ didApiLayer ++ didAuthenticationApiLayer)
  }

}
