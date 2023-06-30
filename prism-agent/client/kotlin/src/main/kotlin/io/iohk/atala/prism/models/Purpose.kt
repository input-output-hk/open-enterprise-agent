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
 * Purpose of the verification material in the DID Document
 *
 * Values: assertionMethod,authentication,capabilityDelegation,capabilityInvocation,keyAgreement
 */

@JsonClass(generateAdapter = false)
enum class Purpose(val value: kotlin.String) {

    @Json(name = "assertionMethod")
    assertionMethod("assertionMethod"),

    @Json(name = "authentication")
    authentication("authentication"),

    @Json(name = "capabilityDelegation")
    capabilityDelegation("capabilityDelegation"),

    @Json(name = "capabilityInvocation")
    capabilityInvocation("capabilityInvocation"),

    @Json(name = "keyAgreement")
    keyAgreement("keyAgreement");

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): String = value

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is Purpose) "$data" else null

        /**
         * Returns a valid [Purpose] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): Purpose? = data?.let {
          val normalizedData = "$it".lowercase()
          values().firstOrNull { value ->
            it == value || normalizedData == "$value".lowercase()
          }
        }
    }
}

