package io.iohk.atala.agent.server.http.marshaller

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import io.iohk.atala.agent.openapi.api.DIDRegistrarApiMarshaller
import io.iohk.atala.agent.openapi.model.{
  CreateManagedDIDResponse,
  CreateManagedDidRequest,
  DIDOperationResponse,
  ErrorResponse,
  ManagedDIDCollection,
  UpdateManagedDIDRequest
}
import spray.json.RootJsonFormat
import zio.*

object DIDRegistrarApiMarshallerImpl extends JsonSupport {

  val layer: ULayer[DIDRegistrarApiMarshaller] = ZLayer.succeed {
    new DIDRegistrarApiMarshaller {
      override implicit def fromEntityUnmarshallerCreateManagedDidRequest
          : FromEntityUnmarshaller[CreateManagedDidRequest] = summon[RootJsonFormat[CreateManagedDidRequest]]

      override implicit def fromEntityUnmarshallerUpdateManagedDIDRequest
          : FromEntityUnmarshaller[UpdateManagedDIDRequest] = summon[RootJsonFormat[UpdateManagedDIDRequest]]

      override implicit def toEntityMarshallerDIDOperationResponse: ToEntityMarshaller[DIDOperationResponse] =
        summon[RootJsonFormat[DIDOperationResponse]]

      override implicit def toEntityMarshallerCreateManagedDIDResponse: ToEntityMarshaller[CreateManagedDIDResponse] =
        summon[RootJsonFormat[CreateManagedDIDResponse]]

      override implicit def toEntityMarshallerManagedDIDCollection: ToEntityMarshaller[ManagedDIDCollection] =
        summon[RootJsonFormat[ManagedDIDCollection]]

      override implicit def toEntityMarshallerErrorResponse: ToEntityMarshaller[ErrorResponse] =
        summon[RootJsonFormat[ErrorResponse]]
    }
  }

}
