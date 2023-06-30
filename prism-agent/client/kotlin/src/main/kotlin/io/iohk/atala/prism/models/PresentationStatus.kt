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

import io.iohk.atala.prism.models.ProofRequestAux

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param presentationId The unique identifier of the presentation record.
 * @param status The current state of the proof presentation record.
 * @param proofs The type of proofs requested in the context of this proof presentation request (e.g., VC schema, trusted issuers, etc.)
 * @param `data` The list of proofs presented by the prover to the verifier.
 * @param connectionId The unique identifier of an established connection between the verifier and the prover.
 */


data class PresentationStatus (

    /* The unique identifier of the presentation record. */
    @Json(name = "presentationId")
    val presentationId: kotlin.String,

    /* The current state of the proof presentation record. */
    @Json(name = "status")
    val status: PresentationStatus.Status,

    /* The type of proofs requested in the context of this proof presentation request (e.g., VC schema, trusted issuers, etc.) */
    @Json(name = "proofs")
    val proofs: kotlin.collections.List<ProofRequestAux>? = null,

    /* The list of proofs presented by the prover to the verifier. */
    @Json(name = "data")
    val `data`: kotlin.collections.List<kotlin.String>? = null,

    /* The unique identifier of an established connection between the verifier and the prover. */
    @Json(name = "connectionId")
    val connectionId: kotlin.String? = null

) {

    /**
     * The current state of the proof presentation record.
     *
     * Values: requestPending,requestSent,requestReceived,requestRejected,presentationPending,presentationGenerated,presentationSent,presentationReceived,presentationVerified,presentationAccepted,presentationRejected,problemReportPending,problemReportSent,problemReportReceived
     */
    @JsonClass(generateAdapter = false)
    enum class Status(val value: kotlin.String) {
        @Json(name = "RequestPending") requestPending("RequestPending"),
        @Json(name = "RequestSent") requestSent("RequestSent"),
        @Json(name = "RequestReceived") requestReceived("RequestReceived"),
        @Json(name = "RequestRejected") requestRejected("RequestRejected"),
        @Json(name = "PresentationPending") presentationPending("PresentationPending"),
        @Json(name = "PresentationGenerated") presentationGenerated("PresentationGenerated"),
        @Json(name = "PresentationSent") presentationSent("PresentationSent"),
        @Json(name = "PresentationReceived") presentationReceived("PresentationReceived"),
        @Json(name = "PresentationVerified") presentationVerified("PresentationVerified"),
        @Json(name = "PresentationAccepted") presentationAccepted("PresentationAccepted"),
        @Json(name = "PresentationRejected") presentationRejected("PresentationRejected"),
        @Json(name = "ProblemReportPending") problemReportPending("ProblemReportPending"),
        @Json(name = "ProblemReportSent") problemReportSent("ProblemReportSent"),
        @Json(name = "ProblemReportReceived") problemReportReceived("ProblemReportReceived");
    }
}

