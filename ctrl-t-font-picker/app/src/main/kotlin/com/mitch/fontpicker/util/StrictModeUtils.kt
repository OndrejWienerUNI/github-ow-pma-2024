package com.mitch.fontpicker.util

import android.os.StrictMode
import com.mitch.fontpicker.BuildConfig
import timber.log.Timber
import java.util.concurrent.Executors

@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
object StrictModeUtils {

    private var originalPolicy: StrictMode.ThreadPolicy? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val _ignoredSubstrings = mutableSetOf<String>()
    val ignoredSubstrings: List<String>
        get() = _ignoredSubstrings.toList()

    // Internal flag to track if the fallback warning has already been logged
    private var hasLoggedFallback = false

    /**
     * Safely retrieves the original StrictMode policy, initializing it if necessary.
     *
     * @return The original StrictMode policy or a default fallback if not initialized.
     */
    private fun getOriginalPolicy(): StrictMode.ThreadPolicy {
        if (originalPolicy == null) {
            if (!hasLoggedFallback) {
                Timber.w("Original StrictMode policy was not initialized. Falling back to default.")
                hasLoggedFallback = true
            }
            return StrictMode.ThreadPolicy.LAX
        }
        return originalPolicy!!
    }

    /**
     * Ensures the original StrictMode policy is captured, initializing it if not already set.
     */
    fun captureOriginalPolicy() {
        if (originalPolicy == null) {
            originalPolicy = StrictMode.getThreadPolicy()
            Timber.i("Captured original StrictMode policy.")
        }
    }

    /**
     * Relax StrictMode policies based on specified parameters.
     *
     * @param diskReads Allow disk reads if true.
     * @param diskWrites Allow disk writes if true.
     * @param network Allow network operations if true.
     */
    fun relax(diskReads: Boolean = false, diskWrites: Boolean = false, network: Boolean = false) {
        if (BuildConfig.DEBUG) {
            captureOriginalPolicy() // Ensure originalPolicy is captured

            val builder = StrictMode.ThreadPolicy.Builder(originalPolicy)
                .detectAll()
                .penaltyLog()
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
            } ?: run {
                Timber.w("StrictMode restore called, but no original policy was saved! Using default policy.")
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
            }
        }
    }

    /**
     * Add substrings to ignore during StrictMode checks.
     * These substrings are matched against the violation stack trace.
     *
     * @param substring Substring to ignore in StrictMode violations.
     */
    fun addIgnoredSubstring(substring: String) {
        _ignoredSubstrings.add(substring)
        Timber.i("Added substring to ignore list: $substring")
        applyFilteredPolicy()
    }

    /**
     * Removes a specific substring from the ignore list.
     *
     * @param substring Substring to remove from the ignored list.
     */
    fun removeIgnoredSubstring(substring: String) {
        if (_ignoredSubstrings.remove(substring)) {
            Timber.i("Removed substring from ignore list: $substring")
            applyFilteredPolicy()
        } else {
            Timber.w("Substring not found in ignore list: $substring")
        }
    }

    /**
     * Clears all ignored substrings and restores the original policy.
     */
    fun clearIgnoredSubstrings() {
        _ignoredSubstrings.clear()
        restore()
        Timber.i("Cleared all ignored substrings.")
    }

    /**
     * Apply the custom StrictMode policy with filtering.
     */
    private fun applyFilteredPolicy() {
        if (BuildConfig.DEBUG) {
            val builder = StrictMode.ThreadPolicy.Builder(getOriginalPolicy())
                .detectAll()
                .penaltyListener(executor) { violation ->
                    val stackTrace = violation.stackTraceToString()
                    val ignoredSubstring = _ignoredSubstrings.firstOrNull { stackTrace.contains(it) }

                    if (ignoredSubstring != null) {
                        Timber.w("Ignored StrictMode violation " +
                                "due to substring '$ignoredSubstring': \n$stackTrace")
                    } else {
                        throw violation
                    }
                }

            StrictMode.setThreadPolicy(builder.build())
            Timber.i(
                "Applied StrictMode policy with substring filtering: " +
                        "\n${_ignoredSubstrings.joinToString()}"
            )
        }
    }
}
