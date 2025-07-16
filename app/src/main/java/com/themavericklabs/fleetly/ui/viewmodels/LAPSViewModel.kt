package com.themavericklabs.fleetly.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.themavericklabs.fleetly.auth.MSALManager
import com.themavericklabs.fleetly.data.models.LAPSResponse
import com.themavericklabs.fleetly.network.GraphAPI
import com.themavericklabs.fleetly.network.NetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LAPSViewModel @Inject constructor(
    private val graphAPI: GraphAPI,
    private val msalManager: MSALManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _lapsResponse = MutableStateFlow<LAPSResponse?>(null)
    val lapsResponse: StateFlow<LAPSResponse?> = _lapsResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<AlertError?>(null)
    val error: StateFlow<AlertError?> = _error.asStateFlow()

    init {
        savedStateHandle.get<String>("deviceId")?.let { deviceId ->
            loadLAPSCredentials(deviceId)
        }
    }

    private fun loadLAPSCredentials(deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _lapsResponse.value = graphAPI.getLapsCredential(deviceId)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun decodePassword(base64: String): String {
        return graphAPI.decodeLAPSPassword(base64)
    }

    private fun handleError(error: Exception) {
        val errorMessage = when (error) {
            is NetworkError.UNAUTHORIZED -> "Authentication error. Please try logging in again."
            is NetworkError.FORBIDDEN -> "You don't have permission to access this resource."
            is NetworkError.NOT_FOUND -> "The requested resource was not found."
            is NetworkError.SERVER_ERROR -> "Server error occurred (Code: ${error.code}). Please try again."
            is NetworkError.GRAPH_ERROR -> error.message
            is NetworkError.UNEXPECTED_ERROR -> "An unexpected error occurred: ${error.message}"
            else -> error.localizedMessage ?: "An unexpected error occurred"
        }
        _error.value = AlertError(errorMessage)
    }
} 