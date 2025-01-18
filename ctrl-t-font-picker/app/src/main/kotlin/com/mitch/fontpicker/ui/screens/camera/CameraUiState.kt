package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri
import androidx.camera.core.CameraSelector
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.data.api.FontResult

sealed interface CameraUiState {

    data class CameraReady(
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK
    ) : CameraUiState

    data object Processing : CameraUiState

    data class Error(
        val errorMessage: String
    ) : CameraUiState

    data class ImageReady(
        val imageUri: Uri
    ) : CameraUiState

    data class FontsReceived(
        val fonts: List<FontResult>
    ) : CameraUiState

    data class DownloadingThumbnails(
        val downloadedFonts: List<FontResult>
    ) : CameraUiState

    data class OpeningFontsDialog(
        val downloadedFonts: List<FontDownloaded>
    ) : CameraUiState

    data class SavingFavoriteFonts(
        val downloadedFonts: List<FontDownloaded>
    ) : CameraUiState

    data class Success(
        val message: String
    ) : CameraUiState
}