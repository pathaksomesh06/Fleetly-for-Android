package com.themavericklabs.fleetly.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.themavericklabs.fleetly.auth.MSALManager
import com.themavericklabs.fleetly.data.models.BitLockerRecoveryKey
import com.themavericklabs.fleetly.data.models.DeviceAction
import com.themavericklabs.fleetly.data.models.DeviceLogCollectionRequest
import com.themavericklabs.fleetly.data.models.GraphResponse
import com.themavericklabs.fleetly.data.models.IntuneDevice
import com.themavericklabs.fleetly.data.models.LAPSResponse
import com.themavericklabs.fleetly.data.models.WipeDeviceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphAPI @Inject constructor(
    private val msalManager: MSALManager
) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://graph.microsoft.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val graphAPIService = retrofit.create(GraphAPIService::class.java)

    private suspend fun getAuthHeader(): String {
        val token = msalManager.getAccessToken()
        return "Bearer $token"
    }

    suspend fun getManagedDevices(platform: String? = null): List<IntuneDevice> = withContext(Dispatchers.IO) {
        try {
            val allDevices = mutableListOf<IntuneDevice>()
            var skip = 0
            val top = 999
            val authHeader = getAuthHeader()
            val filter = platform?.let { 
                when (it.lowercase()) {
                    "windows" -> "deviceType eq 'windowsRT'"
                    "mac" -> "deviceType eq 'macMDM'"
                    "ios" -> "(deviceType eq 'iPad') or (deviceType eq 'iPhone')"
                    "android" -> "(deviceType eq 'android') or (deviceType eq 'androidForWork')"
                    else -> null
                }
            }

            do {
                Log.d(TAG, "Fetching devices with filter: $filter")
                val response = graphAPIService.getManagedDevices(
                    authorization = authHeader,
                    filter = filter,
                    top = top,
                    skip = skip
                )
                Log.d(TAG, "Response count: ${response.value.size}, Total count in response: ${response.toString()}")
                allDevices.addAll(response.value)
                skip += top
            } while (response.nextLink != null)

            allDevices
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices", e)
            handleNetworkError(e)
        }
    }

    suspend fun getManagedDevice(deviceId: String): IntuneDevice = withContext(Dispatchers.IO) {
        try {
            graphAPIService.getManagedDevice(getAuthHeader(), deviceId = deviceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching device details for $deviceId", e)
            handleNetworkError(e)
        }
    }

    suspend fun getFileVaultKey(deviceId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Requesting FileVault key for device: $deviceId")
            val response = graphAPIService.getFileVaultKey(getAuthHeader(), deviceId = deviceId)
            Log.d(TAG, "FileVault key retrieved successfully")
            response.value
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FileVault key for $deviceId", e)
            handleNetworkError(e)
        }
    }

    suspend fun getLapsCredential(deviceId: String): LAPSResponse? = withContext(Dispatchers.IO) {
        try {
            val azureDeviceId = getAzureADDeviceId(deviceId)
            if (azureDeviceId == null) {
                Log.e(TAG, "Could not find Azure AD ID for device $deviceId")
                return@withContext null
            }
            Log.d(TAG, "Fetching LAPS credentials for device: $azureDeviceId")
            graphAPIService.getLAPSCredentials(getAuthHeader(), deviceId = azureDeviceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching LAPS credentials for $deviceId", e)
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "LAPS credentials error response: $errorBody")
            }
            throw handleNetworkError(e)
        }
    }

    suspend fun getBitLockerKeys(deviceId: String): List<BitLockerRecoveryKey> = withContext(Dispatchers.IO) {
        try {
            val azureDeviceId = getAzureADDeviceId(deviceId)
            if (azureDeviceId == null) {
                Log.e(TAG, "Could not find Azure AD ID for device $deviceId")
                return@withContext emptyList()
            }
            val authHeader = getAuthHeader()

            Log.d(TAG, "Fetching BitLocker keys list for Azure device ID: $azureDeviceId")
            val keysResponse = graphAPIService.getBitLockerRecoveryKeys(
                authorization = authHeader,
                filter = "deviceId eq '$azureDeviceId'"
            )

            if (keysResponse.value.isEmpty()) {
                return@withContext emptyList()
            }

            val sortedKeys = keysResponse.value.sortedByDescending { it.createdDateTime }
            val completedKeys = mutableListOf<BitLockerRecoveryKey>()

            sortedKeys.firstOrNull()?.let { latestKey ->
                Log.d(TAG, "Fetching BitLocker key value for key ID: ${latestKey.id}")
                try {
                    val keyDetails = graphAPIService.getBitLockerKey(
                        authorization = authHeader,
                        keyId = latestKey.id
                    )
                    completedKeys.add(keyDetails)
                } catch (e: retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(TAG, "BitLocker key details error response: $errorBody")
                    val errorJson = errorBody?.let { org.json.JSONObject(it).getJSONObject("error") }
                    val errorMessage = errorJson?.getString("message") ?: "Could not retrieve key."
                    throw NetworkError.GRAPH_ERROR(errorMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching details for BitLocker key ID: ${latestKey.id}", e)
                    throw NetworkError.GRAPH_ERROR("Failed to retrieve BitLocker key details.")
                }
            }

            completedKeys
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching BitLocker keys for device ID: $deviceId", e)
            if (e is NetworkError) throw e
            emptyList()
        }
    }

    suspend fun getAzureADDeviceId(intuneDeviceId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting Entra device ID for: $intuneDeviceId")
            val response = graphAPIService.getAzureADDeviceId(getAuthHeader(), deviceId = intuneDeviceId)
            response.azureADDeviceId.also {
                Log.d(TAG, "Found Entra device ID: $it for Intune device $intuneDeviceId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Entra device ID for $intuneDeviceId", e)
            handleNetworkError(e)
        }
    }

    suspend fun performAction(action: DeviceAction, deviceId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Performing action ${action.name} on device: $deviceId")
            val authHeader = getAuthHeader()

            val response = when (action) {
                DeviceAction.SYNC -> graphAPIService.syncDevice(authHeader, deviceId = deviceId)
                DeviceAction.WIPE -> graphAPIService.wipeDevice(authHeader, deviceId = deviceId, body = WipeDeviceRequest())
                DeviceAction.RETIRE -> graphAPIService.retireDevice(authHeader, deviceId = deviceId)
                DeviceAction.FRESH_START -> graphAPIService.freshStart(authHeader, deviceId = deviceId)
                DeviceAction.ROTATE_ADMIN_PASSWORD -> graphAPIService.rotateLocalAdminPassword(authHeader, deviceId = deviceId)
                DeviceAction.COLLECT_DIAGNOSTIC_DATA -> {
                    graphAPIService.collectDiagnosticData(authHeader, deviceId = deviceId, body = DeviceLogCollectionRequest())
                    return@withContext true
                }
                DeviceAction.DELETE -> graphAPIService.deleteDevice(authHeader, deviceId = deviceId)
                DeviceAction.RESTART -> graphAPIService.rebootNow(authHeader, deviceId = deviceId)
                DeviceAction.SHUTDOWN -> graphAPIService.shutDown(authHeader, deviceId = deviceId)
                DeviceAction.REMOTE_LOCK -> graphAPIService.remoteLock(authHeader, deviceId = deviceId)
                DeviceAction.AUTOPILOT_RESET -> graphAPIService.autopilotReset(authHeader, deviceId = deviceId)
            }

            if (response.isSuccessful) {
                Log.d(TAG, "Action ${action.name} completed successfully")
                true
            } else {
                Log.e(TAG, "Error performing action ${action.name} on $deviceId. Code: ${response.code()}, Message: ${response.message()}")
                throw retrofit2.HttpException(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing action ${action.name} on $deviceId", e)
            handleNetworkError(e)
        }
    }

    fun decodeLAPSPassword(base64: String): String {
        return try {
            String(android.util.Base64.decode(base64, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode LAPS password", e)
            "••••••••"
        }
    }

    private fun handleNetworkError(error: Exception): Nothing {
        val networkError = when (error) {
            is retrofit2.HttpException -> {
                when (error.code()) {
                    401 -> NetworkError.UNAUTHORIZED
                    403 -> NetworkError.FORBIDDEN
                    404 -> NetworkError.NOT_FOUND
                    else -> NetworkError.SERVER_ERROR(error.code())
                }
            }
            else -> NetworkError.UNEXPECTED_ERROR(error.message ?: "Unknown error")
        }
        throw networkError
    }

    companion object {
        private const val TAG = "GraphAPI"
    }
}

sealed class NetworkError : Exception() {
    object UNAUTHORIZED : NetworkError()
    object FORBIDDEN : NetworkError()
    object NOT_FOUND : NetworkError()
    data class SERVER_ERROR(val code: Int) : NetworkError()
    data class GRAPH_ERROR(override val message: String) : NetworkError()
    data class UNEXPECTED_ERROR(override val message: String) : NetworkError()
} 