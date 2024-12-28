package com.mitch.christmas.domain.models

enum class ChristmasThemePreference {
    FollowSystem,
    Light,
    Dark;

    companion object {
        val Default: ChristmasThemePreference = FollowSystem
    }
}
