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
import scala.language.implicitConversions

final case class UpdateManagedDIDRequest(
    actions: Seq[UpdateManagedDIDRequestAction]
)

object UpdateManagedDIDRequest {
  given JsonEncoder[UpdateManagedDIDRequest] = DeriveJsonEncoder.gen[UpdateManagedDIDRequest]
  given JsonDecoder[UpdateManagedDIDRequest] = DeriveJsonDecoder.gen[UpdateManagedDIDRequest]
  given Schema[UpdateManagedDIDRequest] = Schema.derived
}

enum ActionType {
  case ADD_KEY extends ActionType
  case REMOVE_KEY extends ActionType
  case ADD_SERVICE extends ActionType
  case REMOVE_SERVICE extends ActionType
  case UPDATE_SERVICE extends ActionType
}

object ActionType {
  given JsonEncoder[ActionType] = JsonEncoder[String].contramap(_.toString)
  given JsonDecoder[ActionType] = JsonDecoder[String].mapOrFail { s =>
    ActionType.values.find(_.toString == s).toRight(s"Unknown action type: $s")
  }
  given Schema[ActionType] = Schema.derivedEnumeration.defaultStringBased
}

@description(UpdateManagedDIDRequestAction.annotations.description)
final case class UpdateManagedDIDRequestAction(
    actionType: ActionType,
    addKey: Option[ManagedDIDKeyTemplate] = None,
    removeKey: Option[RemoveEntryById] = None,
    addService: Option[Service] = None,
    removeService: Option[RemoveEntryById] = None,
    updateService: Option[UpdateManagedDIDServiceAction] = None
)

object UpdateManagedDIDRequestAction {
  object annotations {
    val description =
      """A list of actions to perform on DID document.
        |The field `addKey`, `removeKey`, `addService`, `removeService`, `updateService` must corresponds to
        |the `actionType` specified. For example, `addKey` must be present when `actionType` is `ADD_KEY`.""".stripMargin
  }

  given JsonEncoder[UpdateManagedDIDRequestAction] = DeriveJsonEncoder.gen[UpdateManagedDIDRequestAction]
  given JsonDecoder[UpdateManagedDIDRequestAction] = DeriveJsonDecoder.gen[UpdateManagedDIDRequestAction]
  given Schema[UpdateManagedDIDRequestAction] = Schema.derived

  extension (action: UpdateManagedDIDRequestAction) {
    def toDomain: Either[String, walletDomain.UpdateManagedDIDAction] = {
      import walletDomain.UpdateManagedDIDAction.*
      action.actionType match {
        case ActionType.ADD_KEY =>
          action.addKey
            .toRight("addKey property is missing from action type ADD_KEY")
            .map(template => AddKey(template))
        case ActionType.REMOVE_KEY =>
          action.removeKey
            .toRight("removeKey property is missing from action type REMOVE_KEY")
            .map(i => RemoveKey(i.id))
        case ActionType.ADD_SERVICE =>
          action.addService
            .toRight("addservice property is missing from action type ADD_SERVICE")
            .flatMap(_.toDomain)
            .map(s => AddService(s))
        case ActionType.REMOVE_SERVICE =>
          action.removeService
            .toRight("removeService property is missing from action type REMOVE_SERVICE")
            .map(i => RemoveService(i.id))
        case ActionType.UPDATE_SERVICE =>
          action.updateService
            .toRight("updateService property is missing from action type UPDATE_SERVICE")
            .flatMap(_.toDomain)
            .map(s => UpdateService(s))
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
