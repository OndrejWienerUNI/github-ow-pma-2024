package com.mitch.christmas

import android.app.Application
import com.mitch.christmas.di.DefaultDependenciesProvider
import com.mitch.christmas.di.DependenciesProvider
import timber.log.Timber

class ChristmasApplication : Application() {

    lateinit var dependenciesProvider: DependenciesProvider

    override fun onCreate() {
        super.onCreate()
        dependenciesProvider = DefaultDependenciesProvider(this)

        // BuildConfig will be created after first run of the app
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
