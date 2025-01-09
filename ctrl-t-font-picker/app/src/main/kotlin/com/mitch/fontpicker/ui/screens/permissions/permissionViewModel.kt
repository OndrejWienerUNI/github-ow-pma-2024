package com.mitch.fontpicker.ui.screens.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val permissionsHandler: PermissionsHandler,
    private val onPermissionsGranted: () -> Unit
) : ViewModel() {

    private val _currentPermissionIndex = MutableStateFlow(0)
    val currentPermissionIndex: StateFlow<Int> = _currentPermissionIndex

    private val _allPermissionsGranted = MutableStateFlow(false)
    val allPermissionsGranted: StateFlow<Boolean> = _allPermissionsGranted

    private val _isRequestingPermission = MutableStateFlow(false)
    @Suppress("unused")
    val isRequestingPermission: StateFlow<Boolean> = _isRequestingPermission

    init {
        setupHandlers()
        checkPermissions()
    }

    private fun setupHandlers() {
        permissionsHandler.onPermissionGranted = {
            moveToNextPermission()
        }

        permissionsHandler.onPermissionDenied = {
            _isRequestingPermission.value = false
        }

        permissionsHandler.onAllPermissionsGranted = {
            _allPermissionsGranted.value = true
            onPermissionsGranted() // Trigger navigation to the next screen
        }
    }

    fun checkPermissions() {
        val allGranted = permissionsHandler.isPermissionGrantedForAll()
        _allPermissionsGranted.value = allGranted

        if (allGranted) {
            onPermissionsGranted() // If all permissions are already granted
        }
    }

    fun requestPermission() {
        if (_isRequestingPermission.value || _allPermissionsGranted.value) return

        val currentIndex = _currentPermissionIndex.value
        val (permission, _) = PermissionsHandler.permissionsToRequest[currentIndex]
        _isRequestingPermission.value = true

        viewModelScope.launch {
            permissionsHandler.requestPermission(permission)
        }
    }

    private fun moveToNextPermission() {
        _isRequestingPermission.value = false
        val nextIndex = _currentPermissionIndex.value + 1

        if (nextIndex < PermissionsHandler.permissionsToRequest.size) {
            _currentPermissionIndex.value = nextIndex
        } else {
            _allPermissionsGranted.value = true
        }
    }
}

class PermissionsViewModelFactory(
    private val permissionsHandler: PermissionsHandler,
    private val onPermissionsGranted: () -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionsViewModel(permissionsHandler, onPermissionsGranted) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
