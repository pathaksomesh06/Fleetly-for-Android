package com.themavericklabs.fleetly.network

import com.google.gson.annotations.SerializedName
import com.themavericklabs.fleetly.data.models.BitLockerRecoveryKey
import com.themavericklabs.fleetly.data.models.DeviceLogCollectionRequest
import com.themavericklabs.fleetly.data.models.GraphResponse
import com.themavericklabs.fleetly.data.models.IntuneDevice
import com.themavericklabs.fleetly.data.models.LAPSResponse
import com.themavericklabs.fleetly.data.models.WipeDeviceRequest
import retrofit2.Response
import retrofit2.http.*

interface GraphAPIService {
    
    @GET("beta/deviceManagement/managedDevices")
    suspend fun getManagedDevices(
        @Header("Authorization") authorization: String,
        @Query("\$select") select: String = "id,deviceName,managementAgent,ownerType,complianceState,deviceType,osVersion,lastSyncDateTime,userPrincipalName,deviceRegistrationState,managementState,exchangeAccessState,exchangeAccessStateReason,jailBroken,enrolledDateTime,deviceEnrollmentType,azureActiveDirectoryDeviceId",
        @Query("\$filter") filter: String? = null,
        @Query("\$top") top: Int = 999,
        @Query("\$skip") skip: Int = 0
    ): GraphResponse<List<IntuneDevice>>
    
    @GET("beta/deviceManagement/managedDevices/{deviceId}")
    suspend fun getManagedDevice(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): IntuneDevice
    
    @GET("beta/deviceManagement/managedDevices/{deviceId}/getFileVaultKey")
    suspend fun getFileVaultKey(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): FileVaultResponse
    
    @GET("beta/deviceManagement/managedDevices('{deviceId}')?\$select=azureADDeviceId")
    suspend fun getAzureADDeviceId(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): AzureADDeviceID
    
    @GET("v1.0/directory/deviceLocalCredentials/{deviceId}?\$select=credentials")
    suspend fun getLAPSCredentials(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): LAPSResponse
    
    @GET("beta/informationProtection/bitlocker/recoveryKeys")
    suspend fun getBitLockerRecoveryKeys(
        @Header("Authorization") authorization: String,
        @Query("\$filter") filter: String
    ): GraphResponse<List<BitLockerRecoveryKey>>

    @GET("beta/informationProtection/bitlocker/recoveryKeys/{id}")
    suspend fun getBitLockerKey(
        @Header("Authorization") authorization: String,
        @Path("id") keyId: String,
        @Query("\$select") select: String = "key"
    ): BitLockerRecoveryKey

    // Device Actions
    @POST("beta/deviceManagement/managedDevices/{deviceId}/syncDevice")
    suspend fun syncDevice(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>
    
    @POST("beta/deviceManagement/managedDevices/{deviceId}/wipe")
    suspend fun wipeDevice(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String,
        @Body body: WipeDeviceRequest
    ): Response<Unit>
    
    @POST("beta/deviceManagement/managedDevices/{deviceId}/retire")
    suspend fun retireDevice(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>
    
    @POST("beta/deviceManagement/managedDevices/{deviceId}/freshStart")
    suspend fun freshStart(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>
    
    @POST("beta/deviceManagement/managedDevices/{deviceId}/rotateLocalAdminPassword")
    suspend fun rotateLocalAdminPassword(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>
    
    @POST("beta/deviceManagement/managedDevices/{deviceId}/createDeviceLogCollectionRequest")
    suspend fun collectDiagnosticData(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String,
        @Body body: DeviceLogCollectionRequest
    ): DeviceLogCollectionResponse
    
    @DELETE("beta/deviceManagement/managedDevices/{deviceId}")
    suspend fun deleteDevice(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @POST("beta/deviceManagement/managedDevices/{deviceId}/rebootNow")
    suspend fun rebootNow(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @POST("beta/deviceManagement/managedDevices/{deviceId}/shutDown")
    suspend fun shutDown(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @POST("beta/deviceManagement/managedDevices/{deviceId}/remoteLock")
    suspend fun remoteLock(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @POST("beta/deviceManagement/managedDevices/{deviceId}/autopilotReset")
    suspend fun autopilotReset(
        @Header("Authorization") authorization: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>
}

// Request/Response models
data class FileVaultResponse(
    val value: String
)

data class AzureADDeviceID(
    @SerializedName("azureADDeviceId")
    val azureADDeviceId: String?
)

data class BitLockerKeysResponse(
    val value: List<BitLockerRecoveryKey>
)

data class WipeDeviceRequest(
    @SerializedName("keepEnrollmentData")
    val keepEnrollmentData: Boolean = true,
    @SerializedName("keepUserData")
    val keepUserData: Boolean = false,
    @SerializedName("macOsUnlockCode")
    val macOsUnlockCode: String = "",
    @SerializedName("persistEsimDataPlan")
    val persistEsimDataPlan: Boolean = true
)

data class RetireDeviceRequest(
    @SerializedName("keepEnrollmentData")
    val keepEnrollmentData: Boolean = true,
    @SerializedName("keepUserData")
    val keepUserData: Boolean = true,
    @SerializedName("macOsUnlockCode")
    val macOsUnlockCode: String = "",
    @SerializedName("persistEsimDataPlan")
    val persistEsimDataPlan: Boolean = true
)

data class FreshStartRequest(
    @SerializedName("keepUserData")
    val keepUserData: Boolean = true
)

data class DeviceLogCollectionRequest(
    @SerializedName("@odata.type")
    val odataType: String = "microsoft.graph.deviceLogCollectionRequest",
    @SerializedName("templateType")
    val templateType: String = "predefined"
)

data class DeviceLogCollectionResponse(
    val status: String?
) 