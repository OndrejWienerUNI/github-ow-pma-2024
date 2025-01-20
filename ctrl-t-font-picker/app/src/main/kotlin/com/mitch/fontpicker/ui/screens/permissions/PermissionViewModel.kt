package com.mitch.fontpicker.ui.screens.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class PermissionsViewModel(
    private val permissionsHandler: PermissionsHandler
) : ViewModel() {

    private val _currentPermissionIndex = MutableStateFlow(0)
    val currentPermissionIndex: StateFlow<Int> = _currentPermissionIndex

    val allPermissionsGranted: StateFlow<Boolean> = permissionsHandler.allPermissionsGranted

    private val _isRequestingPermission = MutableStateFlow(false)
    @Suppress("UNUSED")
    val isRequestingPermission: StateFlow<Boolean> = _isRequestingPermission

    init {
        observePermissionResults()
        updateCurrentPermissionIndex()
    }

    private fun observePermissionResults() {
        viewModelScope.launch {
            permissionsHandler.permissionResult.collectLatest { result ->
                result?.let { (permission, granted) ->
                    if (granted) {
                        Timber.i("Permission granted: $permission")
                        updateCurrentPermissionIndex()
                    } else {
                        Timber.i("Permission denied: $permission")
                        handlePermissionDenied()
                    }
                }
            }
        }
    }

    fun requestPermission() {
        if (_isRequestingPermission.value || allPermissionsGranted.value) return

        val currentIndex = _currentPermissionIndex.value
        val (permission, _) = PermissionsHandler.permissionsToRequest[currentIndex]
        _isRequestingPermission.value = true

        permissionsHandler.requestPermission(permission)
    }

    private fun handlePermissionDenied() {
        Timber.i("Permission denied. Staying on the current permission.")
        _isRequestingPermission.value = false
    }

    private fun updateCurrentPermissionIndex() {
        viewModelScope.launch {
            val nextIndex = PermissionsHandler.permissionsToRequest.indexOfFirst {
                !permissionsHandler.isPermissionGranted(it.first)
            }

            if (nextIndex != -1) {
                _currentPermissionIndex.value = nextIndex
            } else {
                Timber.i("PermissionsViewModel: All permissions granted or no more permissions to request.")
            }

            _isRequestingPermission.value = false
        }
    }
}
