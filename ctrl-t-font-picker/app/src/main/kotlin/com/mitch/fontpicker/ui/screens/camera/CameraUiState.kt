package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri
import androidx.camera.core.CameraSelector

sealed interface CameraUiState {
    data object Loading : CameraUiState

    data class Error(
        val error: String
    ) : CameraUiState

    data class CameraReady(
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK
    ) : CameraUiState

    data class ImageReady(
        val imageUri: Uri
    ) : CameraUiState

    data class Success(
        val message: String
    ) : CameraUiState
}
