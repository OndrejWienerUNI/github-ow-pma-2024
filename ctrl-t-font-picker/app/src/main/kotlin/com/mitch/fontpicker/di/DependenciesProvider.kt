package com.mitch.fontpicker.di

import com.mitch.fontpicker.data.room.FontsDatabase
import com.mitch.fontpicker.data.room.repository.FontsDatabaseRepository
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import com.mitch.fontpicker.util.network.NetworkMonitor
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File

interface DependenciesProvider {
    val networkMonitor: NetworkMonitor
    val userSettingsRepository: UserSettingsRepository
    val ioDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val coroutineScope: CoroutineScope
    val database: FontsDatabase
    val databaseRepository: FontsDatabaseRepository
    val httpClient: HttpClient
    val picturesDir: File
    val thumbnailsDir: File
}
