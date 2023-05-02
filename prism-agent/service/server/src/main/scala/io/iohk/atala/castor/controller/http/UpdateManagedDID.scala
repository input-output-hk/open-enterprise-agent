package io.iohk.atala.castor.controller.http

import io.iohk.atala.agent.walletapi.model as walletDomain
import io.iohk.atala.api.http.Annotation
import io.iohk.atala.castor.core.model.did as castorDomain
import io.iohk.atala.castor.core.model.did.ServiceType
import io.iohk.atala.castor.core.util.UriUtils
import io.iohk.atala.shared.utils.Traverse.*
import io.lemonlabs.uri.Uri
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.{description, encodedExample}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonEncoder, JsonDecoder}

final case class UpdateManagedDIDRequest(
    actions: Seq[UpdateManagedDIDRequestAction]
)

object UpdateManagedDIDRequest {
  given JsonEncoder[UpdateManagedDIDRequest] = DeriveJsonEncoder.gen[UpdateManagedDIDRequest]
  given JsonDecoder[UpdateManagedDIDRequest] = DeriveJsonDecoder.gen[UpdateManagedDIDRequest]
  given Schema[UpdateManagedDIDRequest] = Schema.derived
}

final case class UpdateManagedDIDRequestAction(
    @description(UpdateManagedDIDRequestAction.annotations.actionType.description)
    @encodedExample(UpdateManagedDIDRequestAction.annotations.actionType.example)
    actionType: String, // TODO: use enum
    addKey: Option[ManagedDIDKeyTemplate] = None,
    removeKey: Option[RemoveEntryById] = None,
    addService: Option[Service] = None,
    removeService: Option[RemoveEntryById] = None,
    updateService: Option[UpdateManagedDIDServiceAction] = None
)

object UpdateManagedDIDRequestAction {
  object annotations {
    object actionType
        extends Annotation[String](
          description = "The type of action to perform",
          example = "ADD_KEY"
        )
  }

  given JsonEncoder[UpdateManagedDIDRequestAction] = DeriveJsonEncoder.gen[UpdateManagedDIDRequestAction]
  given JsonDecoder[UpdateManagedDIDRequestAction] = DeriveJsonDecoder.gen[UpdateManagedDIDRequestAction]
  given Schema[UpdateManagedDIDRequestAction] = Schema.derived

  extension (action: UpdateManagedDIDRequestAction) {
    def toDomain: Either[String, walletDomain.UpdateManagedDIDAction] = {
      import walletDomain.UpdateManagedDIDAction.*
      action.actionType match {
        case "ADD_KEY" =>
          action.addKey
            .toRight("addKey property is missing from action type ADD_KEY")
            .flatMap(_.toDomain)
            .map(template => AddKey(template))
        case "REMOVE_KEY" =>
          action.removeKey
            .toRight("removeKey property is missing from action type REMOVE_KEY")
            .map(i => RemoveKey(i.id))
        case "ADD_SERVICE" =>
          action.addService
            .toRight("addservice property is missing from action type ADD_SERVICE")
            .flatMap(_.toDomain)
            .map(s => AddService(s))
        case "REMOVE_SERVICE" =>
          action.removeService
            .toRight("removeService property is missing from action type REMOVE_SERVICE")
            .map(i => RemoveService(i.id))
        case "UPDATE_SERVICE" =>
          action.updateService
            .toRight("updateService property is missing from action type UPDATE_SERVICE")
            .flatMap(_.toDomain)
            .map(s => UpdateService(s))
        case s => Left(s"unsupported update DID action type: $s")
      }
    }
  }
}

final case class RemoveEntryById(id: String)

object RemoveEntryById {
  object annotations {
    object id
        extends Annotation[String](
          description = "The id of the entry to remove",
          example = "key-1"
        )
  }

  given JsonEncoder[RemoveEntryById] = DeriveJsonEncoder.gen[RemoveEntryById]
  given JsonDecoder[RemoveEntryById] = DeriveJsonDecoder.gen[RemoveEntryById]
  given Schema[RemoveEntryById] = Schema.derived
}

@description("A patch to existing Service. 'type' and 'serviceEndpoint' cannot both be empty.")
final case class UpdateManagedDIDServiceAction(
    @description(UpdateManagedDIDServiceAction.annotations.id.description)
    @encodedExample(UpdateManagedDIDServiceAction.annotations.id.example)
    id: String,
    @description(UpdateManagedDIDServiceAction.annotations.`type`.description)
    @encodedExample(UpdateManagedDIDServiceAction.annotations.`type`.example)
    `type`: Option[String] = None,
    serviceEndpoint: Option[Seq[String]] = None
)

object UpdateManagedDIDServiceAction {
  object annotations {
    object id
        extends Annotation[String](
          description = "The id of the service to update",
          example = "service-1"
        )

    object `type`
        extends Annotation[String](
          description = "The type of the service",
          example = "LinkedDomains"
        )
  }

  given JsonEncoder[UpdateManagedDIDServiceAction] = DeriveJsonEncoder.gen[UpdateManagedDIDServiceAction]
  given JsonDecoder[UpdateManagedDIDServiceAction] = DeriveJsonDecoder.gen[UpdateManagedDIDServiceAction]
  given Schema[UpdateManagedDIDServiceAction] = Schema.derived

  extension (servicePatch: UpdateManagedDIDServiceAction) {
    def toDomain: Either[String, walletDomain.UpdateServicePatch] =
      for {
        serviceEndpoint <- servicePatch.serviceEndpoint
          .getOrElse(Nil)
          .traverse(s => Uri.parseTry(s).toEither.left.map(_ => s"unable to parse serviceEndpoint $s as URI"))
        normalizedServiceEndpoint <- serviceEndpoint
          .traverse(uri =>
            UriUtils
              .normalizeUri(uri.toString)
              .toRight(s"unable to parse serviceEndpoint ${uri.toString} as URI")
              .map(Uri.parse)
          )
        serviceType <- servicePatch.`type`.fold[Either[String, Option[ServiceType]]](Right(None))(s =>
          castorDomain.ServiceType.parseString(s).toRight(s"unsupported serviceType $s").map(Some(_))
        )
      } yield walletDomain.UpdateServicePatch(
        id = servicePatch.id,
        serviceType = serviceType,
        serviceEndpoints = normalizedServiceEndpoint
      )
  }
}
