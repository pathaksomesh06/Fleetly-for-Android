package com.themavericklabs.fleetly.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.themavericklabs.fleetly.auth.MSALManager
import com.themavericklabs.fleetly.data.models.DeviceAction
import com.themavericklabs.fleetly.data.models.IntuneDevice
import com.themavericklabs.fleetly.network.GraphAPI
import com.themavericklabs.fleetly.network.NetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val graphAPI: GraphAPI,
    private val msalManager: MSALManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _device = MutableStateFlow<IntuneDevice?>(null)
    val device: StateFlow<IntuneDevice?> = _device.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<AlertError?>(null)
    val error: StateFlow<AlertError?> = _error.asStateFlow()

    private val _actionStatus = MutableSharedFlow<String>()
    val actionStatus = _actionStatus.asSharedFlow()

    init {
        savedStateHandle.get<String>("deviceId")?.let { deviceId ->
            loadDevice(deviceId)
        }
    }

    private fun loadDevice(deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _device.value = graphAPI.getManagedDevice(deviceId)
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

    fun performAction(action: DeviceAction) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _device.value?.let {
                    graphAPI.performAction(action, it.id)
                    _actionStatus.emit("${action.displayName} action initiated successfully.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is NetworkError -> e.message ?: "An unknown network error occurred."
                    else -> e.localizedMessage ?: "An unexpected error occurred."
                }
                _actionStatus.emit("Failed to perform action: ${action.displayName}. Error: $errorMessage")
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
} 