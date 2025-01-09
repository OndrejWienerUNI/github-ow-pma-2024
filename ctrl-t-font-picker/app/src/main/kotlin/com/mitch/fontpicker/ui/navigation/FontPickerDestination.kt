package com.mitch.fontpicker.ui.navigation

import kotlinx.serialization.Serializable

sealed interface FontPickerDestination {

    sealed interface Screen : FontPickerDestination {
        @Serializable
        data object Home : Screen
        @Serializable
        data object Permissions : Screen
    }

    @Suppress("Unused")
    sealed interface Graph : FontPickerDestination
}
