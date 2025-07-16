package com.themavericklabs.fleetly.data.models

data class WipeDeviceRequest(
    val keepEnrollmentData: Boolean = false,
    val keepUserData: Boolean = false,
    val macOsUnlockCode: String? = null,
    val persistEsimDataPlan: Boolean = false
) 