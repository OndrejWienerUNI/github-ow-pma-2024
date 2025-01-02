package com.mitch.fontpicker.ui.navigation

import kotlinx.serialization.Serializable

sealed interface FontPickerDestination {

    sealed interface Screen : FontPickerDestination {
        @Serializable
        data object Home : Screen
    }

    sealed interface Graph : FontPickerDestination
}
