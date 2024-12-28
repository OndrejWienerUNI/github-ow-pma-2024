package com.mitch.christmas.ui.navigation

import kotlinx.serialization.Serializable

sealed interface ChristmasDestination {

    sealed interface Screen : ChristmasDestination {
        @Serializable
        data object Home : Screen
    }

    sealed interface Graph : ChristmasDestination
}
