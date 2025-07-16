package com.themavericklabs.fleetly.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.themavericklabs.fleetly.network.GraphAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileVaultViewModel @Inject constructor(
    private val graphAPI: GraphAPI,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _fileVaultKey = MutableStateFlow<String?>(null)
    val fileVaultKey: StateFlow<String?> = _fileVaultKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val deviceId: String = savedStateHandle.get<String>("deviceId")!!

    init {
        fetchFileVaultKey()
    }

    private fun fetchFileVaultKey() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _fileVaultKey.value = graphAPI.getFileVaultKey(deviceId)
            } catch (e: Exception) {
                _error.value = "Failed to fetch FileVault key: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 