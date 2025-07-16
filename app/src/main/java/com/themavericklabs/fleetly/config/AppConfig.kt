package com.themavericklabs.fleetly.config

import android.app.Application
import android.content.Context
import android.content.RestrictionsManager
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor(private val application: Application) {

    val clientID: String?
    var tenantID: String? = "organizations" // Default for multi-tenant
        private set

    init {
        val restrictionsManager = application.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val appRestrictions = restrictionsManager.applicationRestrictions
        clientID = appRestrictions?.getString("client_id")
        Log.d("AppConfig", "Client ID from MDM: $clientID")
    }

    val hasValidMDMConfig: Boolean
        get() = !clientID.isNullOrBlank()

    val requiredScopes = listOf(
        "User.Read",
        "DeviceManagementManagedDevices.ReadWrite.All"
    )

    fun updateTenantID(id: String?) {
        if (!id.isNullOrBlank()) {
            tenantID = id
        }
    }
} 