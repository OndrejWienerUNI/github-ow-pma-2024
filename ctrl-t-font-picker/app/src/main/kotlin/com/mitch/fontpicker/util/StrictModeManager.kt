package com.mitch.fontpicker.util

import android.os.StrictMode
import com.mitch.fontpicker.BuildConfig
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNUSED")
object StrictModeManager {
    private const val TAG = "StrictModeManager"

    /**
     * Array of whitelisted stack traces. If the violation stack trace contains any of these lines, the violations
     * are ignored.
     */
    private val STACKTRACE_WHITELIST = listOf(
        "com.mediatek.scnmodule.ScnModule.isGameApp"
    )
    private val executor = Executors.newSingleThreadExecutor()
    private val violationIdCounter = AtomicInteger(0)

    /**
     * Enables strict mode if necessary based on the build config.
     */
    @JvmStatic
    fun init() {
        Timber.i("$TAG: Initializing StrictModeManager")
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        } else {
            Timber.i("$TAG: StrictMode not enabled, as BuildConfig.DEBUG is false")
        }
    }

    private fun enableStrictMode() {
        Timber.i("$TAG: Enabling StrictMode")

        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyListener(executor) { violation ->
                val violationId = violationIdCounter.incrementAndGet()
                handleViolation(violation, "ThreadPolicy", violationId)
            }
            .build()

        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .detectActivityLeaks()
            .penaltyListener(executor) { violation ->
                val violationId = violationIdCounter.incrementAndGet()
                handleViolation(violation, "VmPolicy", violationId)
            }
            .build()

        StrictMode.setThreadPolicy(threadPolicy)
        StrictMode.setVmPolicy(vmPolicy)

        Timber.i("$TAG: StrictMode enabled")
    }

    /**
     * Handles StrictMode violations, skipping those that match the whitelist.
     * @return `true` if the violation is whitelisted, `false` otherwise.
     */
    private fun handleViolation(violation: Throwable, policyType: String, violationId: Int): Boolean {
        val stackTrace = violation.stackTraceToString()

        val violationType = violation::class.simpleName
        Timber.d(
            "$TAG: Violation detected (ID: $violationId) in $policyType ($violationType). " +
                    "Checking against whitelisted strings..."
        )

        val matchedWhitelistEntry = STACKTRACE_WHITELIST.find { whitelist ->
            stackTrace.contains(whitelist)
        }

        return if (matchedWhitelistEntry != null) {
            val stackTraceLines = stackTrace.lines()
            val matchIndex = stackTraceLines.indexOfFirst { it.contains(matchedWhitelistEntry) }

            val truncatedStackTrace = stackTraceLines
                .take(matchIndex + 1)
                .joinToString("\n")

            val truncationNote = "... (stack trace shortened for clarity)"

            Timber.i(
                "$TAG: Skipping whitelisted violation (ID: $violationId) " +
                        "in $policyType based on match: " +
                        "'$matchedWhitelistEntry'\n$truncatedStackTrace\n$truncationNote"
            )
            true
        } else {
            Timber.e(
                "$TAG: Unhandled StrictMode violation " +
                        "(ID: $violationId) in $policyType ($violationType). " +
                        "Terminating the app."
            )

            // Explicitly terminate the app for unhandled violations
            throw RuntimeException("Unhandled StrictMode $policyType violation (ID: $violationId)", violation)
        }
    }
}
