package com.mitch.fontpicker.ui.screens.camera

sealed interface CameraUiState {
    data object Loading : CameraUiState

    data class Error(
        val error: String? = null
    ) : CameraUiState

    data class Success(
        val cameraData: Int
    ) : CameraUiState
}
