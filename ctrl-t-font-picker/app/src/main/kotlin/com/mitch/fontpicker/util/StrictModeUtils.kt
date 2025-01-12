package com.mitch.fontpicker.util

import android.os.StrictMode
import com.mitch.fontpicker.BuildConfig
import timber.log.Timber

object StrictModeUtils {
    private var originalPolicy: StrictMode.ThreadPolicy? = null

    /**
     * Relax StrictMode policies based on specified parameters.
     *
     * @param diskReads Allow disk reads if true.
     * @param diskWrites Allow disk writes if true.
     * @param network Allow network operations if true.
     */
    fun relax(diskReads: Boolean = false, diskWrites: Boolean = false, network: Boolean = false) {
        if (BuildConfig.DEBUG) {
            originalPolicy = StrictMode.getThreadPolicy()

            val builder = StrictMode.ThreadPolicy.Builder(originalPolicy)
            if (diskReads) builder.permitDiskReads()
            if (diskWrites) builder.permitDiskWrites()
            if (network) builder.permitNetwork()

            StrictMode.setThreadPolicy(builder.build())
            Timber.i("StrictMode relaxed: diskReads=$diskReads, " +
                    "diskWrites=$diskWrites, network=$network")
        }
    }

    /**
     * Restore the original StrictMode policies.
     */
    fun restore() {
        if (BuildConfig.DEBUG) {
            originalPolicy?.let {
                StrictMode.setThreadPolicy(it)
                Timber.i("StrictMode restored to the original policy.")
            } ?: Timber.w("StrictMode restore called, but no original policy was saved!")
        }
    }
}
