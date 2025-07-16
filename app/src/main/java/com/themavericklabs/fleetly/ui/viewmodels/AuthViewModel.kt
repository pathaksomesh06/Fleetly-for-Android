package com.themavericklabs.fleetly.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.themavericklabs.fleetly.auth.AuthError
import com.themavericklabs.fleetly.auth.MSALManager
import com.themavericklabs.fleetly.config.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val msalManager: MSALManager,
    private val appConfig: AppConfig
) : ViewModel() {

    val isAuthenticated: StateFlow<Boolean> = msalManager.isAuthenticated

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<AlertError?>(null)
    val error: StateFlow<AlertError?> = _error.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    init {
        _isConfigured.value = appConfig.hasValidMDMConfig
    }

    fun login(activity: Activity) {
        if (_isLoading.value || !_isConfigured.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                msalManager.authenticate(activity)
            } catch (e: Exception) {
                _error.value = AlertError(e.message ?: "An unknown authentication error occurred.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkInitialAuth() {
        if (!_isConfigured.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                msalManager.silentLogin()
            } catch (e: Exception) {
                // This is expected if the user isn't signed in yet.
                // We don't need to show an error here.
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        viewModelScope.launch {
            msalManager.logout()
        }
    }
} 