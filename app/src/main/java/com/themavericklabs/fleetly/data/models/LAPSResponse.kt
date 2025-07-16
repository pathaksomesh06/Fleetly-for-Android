package com.themavericklabs.fleetly.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class LAPSResponse(
    val credentials: List<LAPSCredential>,
    val deviceName: String?
) : Parcelable

@Parcelize
data class LAPSCredential(
    @SerializedName("accountName")
    val accountName: String?,
    @SerializedName("passwordBase64")
    val passwordBase64: String?,
    @SerializedName("backupDateTime")
    val backupDateTime: Date?
) : Parcelable 