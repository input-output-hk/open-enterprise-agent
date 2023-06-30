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

import io.iohk.atala.prism.models.VerificationPolicyConstraint

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param name 
 * @param description 
 * @param id 
 * @param constraints 
 */


data class VerificationPolicyInput (

    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "description")
    val description: kotlin.String,

    @Json(name = "id")
    val id: java.util.UUID? = null,

    @Json(name = "constraints")
    val constraints: kotlin.collections.List<VerificationPolicyConstraint>? = null

)

