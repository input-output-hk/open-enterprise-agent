/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.iohk.atala.prism.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The invitation for this connection
 *
 * @param id The unique identifier of the invitation. It should be used as parent thread ID (pthid) for the Connection Request message that follows.
 * @param type The DIDComm Message Type URI (MTURI) the invitation message complies with.
 * @param from The DID representing the sender to be used by recipients for future interactions.
 * @param invitationUrl The invitation message encoded as a URL. This URL follows the Out of [Band 2.0 protocol](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) and can be used to generate a QR code for example.
 */


data class ConnectionInvitation (

    /* The unique identifier of the invitation. It should be used as parent thread ID (pthid) for the Connection Request message that follows. */
    @Json(name = "id")
    val id: java.util.UUID,

    /* The DIDComm Message Type URI (MTURI) the invitation message complies with. */
    @Json(name = "type")
    val type: kotlin.String,

    /* The DID representing the sender to be used by recipients for future interactions. */
    @Json(name = "from")
    val from: kotlin.String,

    /* The invitation message encoded as a URL. This URL follows the Out of [Band 2.0 protocol](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) and can be used to generate a QR code for example. */
    @Json(name = "invitationUrl")
    val invitationUrl: kotlin.String

)

