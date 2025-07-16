package com.themavericklabs.fleetly.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class IntuneDevice(
    val id: String,
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("managementAgent")
    val managementAgent: String,
    @SerializedName("ownerType")
    val ownerType: String,
    @SerializedName("complianceState")
    val complianceState: String,
    @SerializedName("deviceType")
    val deviceType: String,
    @SerializedName("osVersion")
    val osVersion: String?,
    @SerializedName("lastSyncDateTime")
    val lastSyncDateTime: Date,
    @SerializedName("userPrincipalName")
    val userPrincipalName: String?,
    @SerializedName("deviceRegistrationId")
    val deviceRegistrationId: String?,
    @SerializedName("azureActiveDirectoryDeviceId")
    val azureActiveDirectoryDeviceId: String?,
    @SerializedName("deviceRegistrationState")
    val deviceRegistrationState: String,
    @SerializedName("managementState")
    val managementState: String,
    @SerializedName("exchangeAccessState")
    val exchangeAccessState: String,
    @SerializedName("exchangeAccessStateReason")
    val exchangeAccessStateReason: String,
    @SerializedName("jailBroken")
    val jailBroken: String?,
    @SerializedName("enrolledDateTime")
    val enrolledDateTime: Date,
    @SerializedName("deviceEnrollmentType")
    val deviceEnrollmentType: String,
    @SerializedName("deviceActionResults")
    val deviceActionResults: List<DeviceActionResult>?
) : Parcelable {

    @Parcelize
    data class DeviceActionResult(
        @SerializedName("actionName")
        val actionName: String,
        @SerializedName("actionState")
        val actionState: String,
        @SerializedName("startDateTime")
        val startDateTime: Date,
        @SerializedName("lastUpdatedDateTime")
        val lastUpdatedDateTime: Date
    ) : Parcelable

    // Computed properties
    val name: String get() = deviceName
    val complianceStatus: String get() = complianceState
    val lastSync: Date get() = lastSyncDateTime
    val enrollmentDate: Date get() = enrolledDateTime
    
    val isCompliant: Boolean
        get() = complianceState.lowercase() == "compliant"
    
    val isJailbroken: Boolean
        get() = jailBroken?.lowercase() in listOf("true", "yes")
    
    val manufacturer: String
        get() = when (deviceType.lowercase()) {
            "macmdm", "ipad", "iphone" -> "Apple"
            "windowsrt", "windows" -> "Microsoft"
            "androidforwork", "android" -> "Android"
            else -> deviceType
        }
    
    val model: String
        get() = when (deviceType.lowercase()) {
            "macmdm" -> "Mac"
            "ipad" -> "iPad"
            "iphone" -> "iPhone"
            "windowsrt", "windows" -> "Windows"
            "androidforwork", "android" -> "Android"
            else -> deviceType
        }
    
    // Fallback to Intune device ID if azureActiveDirectoryDeviceId is missing
    val azureADDeviceId: String
        get() = azureActiveDirectoryDeviceId ?: id
} 