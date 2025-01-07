package com.mitch.fontpicker.ui.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Loading)
    val uiState: StateFlow<CameraUiState> = _uiState

    init {
        loadCameraData()
    }

    private fun loadCameraData() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(3000) // Simulated delay of 3 seconds
                _uiState.value = CameraUiState.Success(
                    cameraData = R.string.screen_desc_camera
                )
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error(error = e.message)
            }
        }
    }

    // Stub for shooting a photo
    fun shootPhoto() {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            kotlinx.coroutines.delay(1000) // Simulate a delay for photo capture
            _uiState.value = CameraUiState.Success(R.string.photo_captured)
        }
    }

    // Stub for flipping the camera
    fun flipCamera() {
        _uiState.value = CameraUiState.Success(R.string.camera_flipped)
    }

    // Stub for opening the gallery
    fun openGallery() {
        _uiState.value = CameraUiState.Success(R.string.gallery_opened)
    }
}
