package com.mitch.fontpicker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.mitch.fontpicker.BuildConfig
import com.mitch.fontpicker.data.encrypted
import com.mitch.fontpicker.data.room.FontsDatabase
import com.mitch.fontpicker.data.room.FontsDatabaseInstance
import com.mitch.fontpicker.data.room.repository.FontsDatabaseRepository
import com.mitch.fontpicker.data.settings.DefaultUserSettingsRepository
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import com.mitch.fontpicker.data.userprefs.UserPreferencesLocalDataSource
import com.mitch.fontpicker.data.userprefs.UserPreferencesProtoModel
import com.mitch.fontpicker.data.userprefs.UserPreferencesSerializer
import com.mitch.fontpicker.util.network.ConnectivityManagerNetworkMonitor
import com.mitch.fontpicker.util.network.NetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

class DefaultDependenciesProvider(
    private val context: Context
) : DependenciesProvider {

    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(
            context = context,
            ioDispatcher = ioDispatcher
        )
    }

    private val preferencesDataStore: DataStore<UserPreferencesProtoModel> by lazy {
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer.encrypted(),
            scope = CoroutineScope(coroutineScope.coroutineContext + ioDispatcher)
        ) {
            context.dataStoreFile("user_preferences.pb")
        }
    }

    override val userSettingsRepository: UserSettingsRepository by lazy {
        DefaultUserSettingsRepository(
            userPreferencesLocalDataSource = UserPreferencesLocalDataSource(preferencesDataStore)
        )
    }

    override val ioDispatcher: CoroutineDispatcher by lazy {
        Dispatchers.IO
    }

    override val defaultDispatcher: CoroutineDispatcher by lazy {
        Dispatchers.Default
    }

    override val coroutineScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

    override val database: FontsDatabase by lazy {
        FontsDatabaseInstance.getDatabase(context)
    }

    override val databaseRepository: FontsDatabaseRepository by lazy {
        FontsDatabaseRepository(database)
    }

    private val externalFilesDir: File by lazy {
        runBlocking(Dispatchers.IO) {
            context.getExternalFilesDir(null)
                ?: throw IllegalStateException("Cannot access external files directory")
        }
    }

    override val picturesDir: File
        get() = File(externalFilesDir, "pictures")

    override val thumbnailsDir: File
        get() = File(externalFilesDir, "thumbnails")

    private val jsonSerializer: Json by lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    @Suppress("Unused")
    override val httpClient: HttpClient by lazy {
        HttpClient {
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = Logger.ANDROID
                    level = LogLevel.INFO
                }
            }
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(Resources)
            install(Auth)
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
