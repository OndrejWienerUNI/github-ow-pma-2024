package com.mitch.christmas.di

import com.mitch.christmas.data.ChristmasDatabase
import com.mitch.christmas.data.settings.UserSettingsRepository
import com.mitch.christmas.util.network.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

interface DependenciesProvider {
    val networkMonitor: NetworkMonitor
    val userSettingsRepository: UserSettingsRepository
    val ioDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val coroutineScope: CoroutineScope
    val database: ChristmasDatabase
}
