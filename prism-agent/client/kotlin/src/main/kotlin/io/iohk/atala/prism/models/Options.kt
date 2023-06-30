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
 * The options to use when creating the proof presentation request (e.g., domain, challenge).
 *
 * @param challenge The challenge should be a randomly generated string.
 * @param domain The domain value can be any string or URI.
 */


data class Options (

    /* The challenge should be a randomly generated string. */
    @Json(name = "challenge")
    val challenge: kotlin.String,

    /* The domain value can be any string or URI. */
    @Json(name = "domain")
    val domain: kotlin.String

)

