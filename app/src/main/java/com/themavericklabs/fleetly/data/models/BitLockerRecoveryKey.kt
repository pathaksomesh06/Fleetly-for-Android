package com.themavericklabs.fleetly.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class BitLockerRecoveryKey(
    val id: String,
    @SerializedName("createdDateTime")
    val createdDateTime: Date,
    @SerializedName("volumeType")
    val volumeType: String?,
    @SerializedName("deviceId")
    val deviceId: String,
    val key: String?, // This might be nil in the list response
    @SerializedName("driveLabel")
    val driveLabel: String?
) : Parcelable {
    
    // For backward compatibility
    val recoveryKey: String
        get() = key ?: "Key not retrieved"
} 