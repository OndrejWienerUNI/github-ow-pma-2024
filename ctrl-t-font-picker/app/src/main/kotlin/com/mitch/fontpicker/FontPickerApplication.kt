package com.mitch.fontpicker

import android.app.Application
import com.mitch.fontpicker.di.DefaultDependenciesProvider
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.util.StrictModeManager
import timber.log.Timber

class FontPickerApplication : Application() {

    lateinit var dependenciesProvider: DependenciesProvider

    override fun onCreate() {
        super.onCreate()
        dependenciesProvider = DefaultDependenciesProvider(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            // Initialize StrictModeManager
            StrictModeManager.init()
        }
    }
}
