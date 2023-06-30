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

package io.iohk.atala.prism.apis

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.HttpUrl

import io.iohk.atala.prism.models.AcceptConnectionInvitationRequest
import io.iohk.atala.prism.models.Connection
import io.iohk.atala.prism.models.ConnectionsPage
import io.iohk.atala.prism.models.CreateConnectionRequest
import io.iohk.atala.prism.models.ErrorResponse

import com.squareup.moshi.Json

import io.iohk.atala.prism.infrastructure.ApiClient
import io.iohk.atala.prism.infrastructure.ApiResponse
import io.iohk.atala.prism.infrastructure.ClientException
import io.iohk.atala.prism.infrastructure.ClientError
import io.iohk.atala.prism.infrastructure.ServerException
import io.iohk.atala.prism.infrastructure.ServerError
import io.iohk.atala.prism.infrastructure.MultiValueMap
import io.iohk.atala.prism.infrastructure.PartConfig
import io.iohk.atala.prism.infrastructure.RequestConfig
import io.iohk.atala.prism.infrastructure.RequestMethod
import io.iohk.atala.prism.infrastructure.ResponseType
import io.iohk.atala.prism.infrastructure.Success
import io.iohk.atala.prism.infrastructure.toMultiValue

class ConnectionsManagementApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * Accepts an Out of Band invitation.
     *  Accepts an [Out of Band 2.0](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) invitation, generates a new Peer DID, and submits a Connection Request to the inviter. It returns a connection object in &#x60;ConnectionRequestPending&#x60; state, until the Connection Request is eventually sent to the inviter by the prism-agent&#39;s background process. The connection object state will then automatically move to &#x60;ConnectionRequestSent&#x60;. 
     * @param acceptConnectionInvitationRequest The request used by an invitee to accept a connection invitation received from an inviter, using out-of-band mechanism.
     * @return Connection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun acceptConnectionInvitation(acceptConnectionInvitationRequest: AcceptConnectionInvitationRequest) : Connection {
        val localVarResponse = acceptConnectionInvitationWithHttpInfo(acceptConnectionInvitationRequest = acceptConnectionInvitationRequest)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as Connection
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Accepts an Out of Band invitation.
     *  Accepts an [Out of Band 2.0](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) invitation, generates a new Peer DID, and submits a Connection Request to the inviter. It returns a connection object in &#x60;ConnectionRequestPending&#x60; state, until the Connection Request is eventually sent to the inviter by the prism-agent&#39;s background process. The connection object state will then automatically move to &#x60;ConnectionRequestSent&#x60;. 
     * @param acceptConnectionInvitationRequest The request used by an invitee to accept a connection invitation received from an inviter, using out-of-band mechanism.
     * @return ApiResponse<Connection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun acceptConnectionInvitationWithHttpInfo(acceptConnectionInvitationRequest: AcceptConnectionInvitationRequest) : ApiResponse<Connection?> {
        val localVariableConfig = acceptConnectionInvitationRequestConfig(acceptConnectionInvitationRequest = acceptConnectionInvitationRequest)

        return request<AcceptConnectionInvitationRequest, Connection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation acceptConnectionInvitation
     *
     * @param acceptConnectionInvitationRequest The request used by an invitee to accept a connection invitation received from an inviter, using out-of-band mechanism.
     * @return RequestConfig
     */
    fun acceptConnectionInvitationRequestConfig(acceptConnectionInvitationRequest: AcceptConnectionInvitationRequest) : RequestConfig<AcceptConnectionInvitationRequest> {
        val localVariableBody = acceptConnectionInvitationRequest
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/connection-invitations",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Creates a new connection record and returns an Out of Band invitation.
     *  Generates a new Peer DID and creates an [Out of Band 2.0](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) invitation. It returns a new connection record in &#x60;InvitationGenerated&#x60; state. The request body may contain a &#x60;label&#x60; that can be used as a human readable alias for the connection, for example &#x60;{&#39;label&#39;: \&quot;Bob\&quot;}&#x60; 
     * @param createConnectionRequest JSON object required for the connection creation
     * @return Connection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun createConnection(createConnectionRequest: CreateConnectionRequest) : Connection {
        val localVarResponse = createConnectionWithHttpInfo(createConnectionRequest = createConnectionRequest)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as Connection
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Creates a new connection record and returns an Out of Band invitation.
     *  Generates a new Peer DID and creates an [Out of Band 2.0](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) invitation. It returns a new connection record in &#x60;InvitationGenerated&#x60; state. The request body may contain a &#x60;label&#x60; that can be used as a human readable alias for the connection, for example &#x60;{&#39;label&#39;: \&quot;Bob\&quot;}&#x60; 
     * @param createConnectionRequest JSON object required for the connection creation
     * @return ApiResponse<Connection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun createConnectionWithHttpInfo(createConnectionRequest: CreateConnectionRequest) : ApiResponse<Connection?> {
        val localVariableConfig = createConnectionRequestConfig(createConnectionRequest = createConnectionRequest)

        return request<CreateConnectionRequest, Connection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation createConnection
     *
     * @param createConnectionRequest JSON object required for the connection creation
     * @return RequestConfig
     */
    fun createConnectionRequestConfig(createConnectionRequest: CreateConnectionRequest) : RequestConfig<CreateConnectionRequest> {
        val localVariableBody = createConnectionRequest
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/connections",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Gets an existing connection record by its unique identifier.
     * Gets an existing connection record by its unique identifier
     * @param connectionId The unique identifier of the connection record.
     * @return Connection
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getConnection(connectionId: java.util.UUID) : Connection {
        val localVarResponse = getConnectionWithHttpInfo(connectionId = connectionId)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as Connection
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Gets an existing connection record by its unique identifier.
     * Gets an existing connection record by its unique identifier
     * @param connectionId The unique identifier of the connection record.
     * @return ApiResponse<Connection?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getConnectionWithHttpInfo(connectionId: java.util.UUID) : ApiResponse<Connection?> {
        val localVariableConfig = getConnectionRequestConfig(connectionId = connectionId)

        return request<Unit, Connection>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation getConnection
     *
     * @param connectionId The unique identifier of the connection record.
     * @return RequestConfig
     */
    fun getConnectionRequestConfig(connectionId: java.util.UUID) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/connections/{connectionId}".replace("{"+"connectionId"+"}", encodeURIComponent(connectionId.toString())),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    /**
     * Gets the list of connection records.
     * Get the list of connection records paginated
     * @param offset  (optional)
     * @param limit  (optional)
     * @return ConnectionsPage
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getConnections(offset: kotlin.Int? = null, limit: kotlin.Int? = null) : ConnectionsPage {
        val localVarResponse = getConnectionsWithHttpInfo(offset = offset, limit = limit)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as ConnectionsPage
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * Gets the list of connection records.
     * Get the list of connection records paginated
     * @param offset  (optional)
     * @param limit  (optional)
     * @return ApiResponse<ConnectionsPage?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getConnectionsWithHttpInfo(offset: kotlin.Int?, limit: kotlin.Int?) : ApiResponse<ConnectionsPage?> {
        val localVariableConfig = getConnectionsRequestConfig(offset = offset, limit = limit)

        return request<Unit, ConnectionsPage>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation getConnections
     *
     * @param offset  (optional)
     * @param limit  (optional)
     * @return RequestConfig
     */
    fun getConnectionsRequestConfig(offset: kotlin.Int?, limit: kotlin.Int?) : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf<kotlin.String, kotlin.collections.List<kotlin.String>>()
            .apply {
                if (offset != null) {
                    put("offset", listOf(offset.toString()))
                }
                if (limit != null) {
                    put("limit", listOf(limit.toString()))
                }
            }
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/connections",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
