package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri

sealed interface CameraUiState {
    data object Loading : CameraUiState

    data class Error(
        val error: String? = null
    ) : CameraUiState

    data class Success(
        val photoUri: Uri? = null,      // For captured photos
        val galleryUri: Uri? = null,   // For selected gallery images
        val message: String? = null    // For general success messages
    ) : CameraUiState
}
