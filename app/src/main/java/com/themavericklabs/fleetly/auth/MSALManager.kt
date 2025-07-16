package com.themavericklabs.fleetly.auth

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.themavericklabs.fleetly.config.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import com.microsoft.identity.client.exception.MsalUiRequiredException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest

@Singleton
class MSALManager @Inject constructor(
    private val application: Application,
    private val appConfig: AppConfig
) {
    private var publicClientApplication: ISingleAccountPublicClientApplication? = null
    private val initMutex = Mutex()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private suspend fun getOrCreatePCA(): ISingleAccountPublicClientApplication {
        publicClientApplication?.let { return it }

        return initMutex.withLock {
            // Check again in case it was initialized while waiting for the lock
            publicClientApplication?.let { return it }

            val newPCA = suspendCancellableCoroutine { continuation ->
                if (!appConfig.hasValidMDMConfig) {
                    _isConfigured.value = false
                    continuation.resumeWithException(AuthError.NotInitialized)
                    return@suspendCancellableCoroutine
                }

                val redirectUri = try {
                    generateRedirectUri()
                } catch (e: Exception) {
                    Log.e("MSALManager", "Could not generate Redirect URI.", e)
                    continuation.resumeWithException(e)
                    return@suspendCancellableCoroutine
                }

                val audienceType = if (appConfig.tenantID == "organizations") "AzureADMultipleOrgs" else "AzureADMyOrg"
                val audienceJson = JSONObject().put("type", audienceType)
                if (audienceType == "AzureADMyOrg") {
                    audienceJson.put("tenant_id", appConfig.tenantID)
                }

                val authority = JSONObject()
                    .put("type", "AAD")
                    .put("audience", audienceJson)

                val configJson = JSONObject()
                    .put("client_id", appConfig.clientID)
                    .put("authorization_user_agent", "DEFAULT")
                    .put("redirect_uri", redirectUri)
                    .put("account_mode", "SINGLE")
                    .put("authorities", JSONArray().put(authority))
                    .toString()

                val configFile = File(application.cacheDir, "msal_config.json")
                configFile.writeText(configJson)

                PublicClientApplication.createSingleAccountPublicClientApplication(
                    application,
                    configFile,
                    object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            _isConfigured.value = true
                            if (continuation.isActive) continuation.resume(application)
                        }

                        override fun onError(exception: MsalException) {
                            _isConfigured.value = false
                            if (continuation.isActive) continuation.resumeWithException(exception)
                        }
                    }
                )
            }
            publicClientApplication = newPCA
            newPCA
        }
    }

    suspend fun authenticate(activity: Activity) {
        val pca = getOrCreatePCA()
        val scopes = appConfig.requiredScopes
        if (scopes.isEmpty()) {
            throw AuthError.NoScopes
        }

        return suspendCancellableCoroutine { continuation ->
            val authCallback = object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    val tenantId = authenticationResult.account.claims?.get("tid") as? String
                    if (tenantId != null) {
                        appConfig.updateTenantID(tenantId)
                        Log.d("MSALManager", "Tenant ID updated to: $tenantId")
                    }
                    _isAuthenticated.value = true
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                override fun onError(exception: MsalException) {
                    _isAuthenticated.value = false
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }

                override fun onCancel() {
                    _isAuthenticated.value = false
                    if (continuation.isActive) {
                        continuation.resumeWithException(AuthError.Cancelled)
                    }
                }
            }

            val signInParameters = SignInParameters.builder()
                .withActivity(activity)
                .withScopes(scopes)
                .withCallback(authCallback)
                .build()

            pca.signIn(signInParameters)
        }
    }

    suspend fun silentLogin() {
        val pca = getOrCreatePCA()
        withContext(Dispatchers.IO) {
            try {
                val account = pca.currentAccount?.currentAccount ?: throw AuthError.NoAccount
                val scopes = appConfig.requiredScopes
                if (scopes.isEmpty()) {
                    throw AuthError.NoScopes
                }
                val silentParameters = AcquireTokenSilentParameters.Builder()
                    .forAccount(account)
                    .fromAuthority(account.authority)
                    .withScopes(scopes)
                    .build()
                val authResult = pca.acquireTokenSilent(silentParameters)
                _isAuthenticated.value = true
                Log.d("MSALManager", "Silent login successful. Expiry: ${authResult?.expiresOn}")
            } catch (e: MsalException) {
                _isAuthenticated.value = false
                throw e
            }
        }
    }

    suspend fun getAccessToken(): String {
        val pca = getOrCreatePCA()
        val account = withContext(Dispatchers.IO) { pca.currentAccount?.currentAccount } ?: throw AuthError.NoAccount
        val scopes = appConfig.requiredScopes
        if (scopes.isEmpty()) {
            throw AuthError.NoScopes
        }
        val silentParameters = AcquireTokenSilentParameters.Builder()
            .forAccount(account)
            .fromAuthority(account.authority)
            .withScopes(scopes)
            .build()
        return try {
            val authResult = withContext(Dispatchers.IO) { pca.acquireTokenSilent(silentParameters) }
            authResult.accessToken
        } catch (e: MsalUiRequiredException) {
            throw AuthError.InteractionRequired("Interaction required for token refresh")
        } catch (e: MsalException) {
            throw AuthError.TokenRefreshFailed(e.localizedMessage ?: "Failed to refresh token")
        }
    }

    suspend fun logout() {
        // Don't initialize just to log out. If PCA is null, there's nothing to do.
        publicClientApplication?.signOut()
        _isAuthenticated.value = false
    }

    @Suppress("DEPRECATION")
    private fun generateRedirectUri(): String {
        val packageName = application.packageName
        val info: PackageInfo = application.packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNATURES
        )
        val signature = info.signatures[0]
        val md = MessageDigest.getInstance("SHA")
        md.update(signature.toByteArray())
        val signatureHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        val encodedSignatureHash = URLEncoder.encode(signatureHash, "UTF-8")

        return "msauth://$packageName/$encodedSignatureHash"
    }
}

sealed class AuthError(message: String) : Exception(message) {
    object NotInitialized : AuthError("MSAL client not initialized. Check configuration.")
    object NoAccount : AuthError("No account found. Please sign in again.")
    object NoScopes : AuthError("No authentication scopes are configured.")
    object Cancelled : AuthError("Authentication was cancelled.")
    class InteractionRequired(details: String) : AuthError("Interaction required: $details")
    class TokenRefreshFailed(details: String) : AuthError("Token refresh failed: $details")
}