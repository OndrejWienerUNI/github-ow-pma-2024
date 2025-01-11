    package com.mitch.fontpicker.ui.screens.permissions

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import timber.log.Timber

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
                Timber.i("PermissionsHandler: All permissions granted callback invoked.")
                _allPermissionsGranted.value = true
                onPermissionsGranted() // Trigger navigation to the next screen
            }
        }

        private fun checkPermissions() {
            viewModelScope.launch {
                val allGranted = permissionsHandler.isPermissionGrantedForAll()

                if (!allGranted) {
                    val nextPermissionIndex = PermissionsHandler.permissionsToRequest.indexOfFirst {
                        !permissionsHandler.isPermissionGranted(it.first)
                    }
                    _currentPermissionIndex.value = nextPermissionIndex
                }

                _allPermissionsGranted.value = allGranted

                if (allGranted) {
                    Timber.i("PermissionsViewModel: All permissions are granted.")
                    onPermissionsGranted()
                } else {
                    Timber.i("PermissionsViewModel: Some permissions are still denied.")
                }
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

            // Increment the index to find the next permission to request
            val nextIndex = (currentPermissionIndex.value + 1 until PermissionsHandler.permissionsToRequest.size)
                .firstOrNull { index ->
                    val (permission, _) = PermissionsHandler.permissionsToRequest[index]
                    !permissionsHandler.isPermissionGranted(permission)
                }

            if (nextIndex != null) {
                _currentPermissionIndex.value = nextIndex
            } else {
                Timber.i("PermissionsViewModel: All permissions granted.")
                _allPermissionsGranted.value = true
                onPermissionsGranted()
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
