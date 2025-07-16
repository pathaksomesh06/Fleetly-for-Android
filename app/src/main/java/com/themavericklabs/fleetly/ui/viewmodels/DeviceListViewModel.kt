package com.themavericklabs.fleetly.ui.viewmodels

import android.app.Activity
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val graphAPI: GraphAPI,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _allDevices = MutableStateFlow<List<IntuneDevice>>(emptyList())
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<AlertError?>(null)
    val error: StateFlow<AlertError?> = _error.asStateFlow()

    val filteredDevices: StateFlow<List<IntuneDevice>> =
        combine(_allDevices, _searchText) { devices, search ->
            if (search.isBlank()) {
                devices
            } else {
                devices.filter {
                    it.name.contains(search, ignoreCase = true) ||
                            it.userPrincipalName?.contains(search, ignoreCase = true) ?: false
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            val platform = savedStateHandle.get<String>("platform")
            try {
                _allDevices.value = graphAPI.getManagedDevices(platform)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncAllDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allDevices.value.forEach { device ->
                    graphAPI.performAction(DeviceAction.SYNC, device.id)
                }
                loadDevices() // Refresh the list
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
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
} 