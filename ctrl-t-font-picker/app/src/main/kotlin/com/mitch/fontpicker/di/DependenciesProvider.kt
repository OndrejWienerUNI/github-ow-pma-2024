package com.mitch.fontpicker.di

import com.mitch.fontpicker.data.FontPickerDatabase
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import com.mitch.fontpicker.util.network.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File

interface DependenciesProvider {
    val networkMonitor: NetworkMonitor
    val userSettingsRepository: UserSettingsRepository
    val ioDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val coroutineScope: CoroutineScope
    val database: FontPickerDatabase
    val picturesDir: File
}
