package com.mitch.fontpicker

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import com.mitch.fontpicker.di.DefaultDependenciesProvider
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.util.StrictModeUtils
import timber.log.Timber

class FontPickerApplication : Application() {

    lateinit var dependenciesProvider: DependenciesProvider

    override fun onCreate() {
        super.onCreate()
        dependenciesProvider = DefaultDependenciesProvider(this)

        // BuildConfig will be created after first run of the app
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            setStrictModePolicy()
        }

        // Device specific ignored violations
        StrictModeUtils.captureOriginalPolicy()
        StrictModeUtils.addIgnoredSubstring("ScnModule.isGameApp")
    }

    /**
     * Set a thread policy that detects all potential problems on the main thread, such as network
     * and disk access.
     *
     * If a problem is found, the offending call will be logged and the application will be killed.
     */
    private fun setStrictModePolicy() {
        StrictMode.setThreadPolicy(
            Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }
}
