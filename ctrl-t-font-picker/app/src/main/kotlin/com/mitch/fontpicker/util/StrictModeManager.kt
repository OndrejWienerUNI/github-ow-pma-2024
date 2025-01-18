package com.mitch.fontpicker.util

import android.os.StrictMode
import com.mitch.fontpicker.BuildConfig
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages StrictMode policies for the application, enabling detection of potentially harmful operations
 * during development. It allows configuring custom whitelists for stack traces to ignore specific
 * violations that are deemed acceptable or unavoidable.
 *
 * **Usage:**
 * Initialize the `StrictModeManager` early in the application lifecycle, typically in the `onCreate`
 * method of your `Application` class. You can add or remove whitelist entries as needed to suppress
 * known benign violations.
 *
 * **Example:**
 * ```kotlin
 * if (BuildConfig.DEBUG) {
 *     Timber.plant(Timber.DebugTree())
 *     StrictModeManager.addWhitelistEntries(
 *         listOf(
 *             "com.mediatek.scnmodule.ScnModule.isGameApp",
 *             "androidx.core.content.FileProvider.parsePathStrategy"
 *         )
 *     )
 *     StrictModeManager.initializeStrictMode()
 * }
 * ```
 *
 * **Note:**
 * - StrictMode is enabled only in debug builds to aid in identifying and fixing issues during development.
 * - Whitelisted stack traces help in ignoring known safe violations, preventing unnecessary noise in logs.
 */
@Suppress("UNUSED")
object StrictModeManager {
    private const val TAG = "StrictModeManager"

    private val executor = Executors.newSingleThreadExecutor()
    private val violationIdCounter = AtomicInteger(0)
    private val stackTraceWhiteList = mutableListOf<String>()
    private var isStrictModeInitialized: Boolean = false

    private const val WL_ADD_CLARIFICATION_MESSAGE: String
        = "initializeStrictMode has to be called for StrictMode " +
          "to be enabled and this taking any effect." +
          "\nIf you're calling this on app start, it's preferred to call initializeStrictMode" +
          " directly after this."

    private const val WL_REMOVE_CLARIFICATION_MESSAGE: String
        = "Unless initializeStrictMode is called (preferably on app start), StrictMode " +
          "will not be enabled and the white list settings won't do anything at all."

    @JvmStatic
    fun initializeStrictMode() {
        Timber.i("$TAG: Initializing StrictModeManager")
        if (BuildConfig.DEBUG) {
            Timber.i("$TAG: Enabling StrictMode (app is in BuildConfig.DEBUG)\n" +
                    "Current stack trace white list = $stackTraceWhiteList")
            enableStrictMode()
            isStrictModeInitialized = true
        } else {
            Timber.i("$TAG: StrictMode not enabled, as BuildConfig.DEBUG is false")
        }
    }

    @JvmStatic
    val whiteList: List<String>
        get() = stackTraceWhiteList.toList()


    @JvmStatic
    @Synchronized
    fun addWhitelistEntries(entries: List<String>) {
        stackTraceWhiteList.addAll(entries)
        Timber.i("$TAG: Adding entries to " +
                "stack trace white list: $entries\n" +
                "New stack trace white list = $stackTraceWhiteList" +
                if (!isStrictModeInitialized) "\n$WL_ADD_CLARIFICATION_MESSAGE" else "")
    }

    @JvmStatic
    @Synchronized
    fun removeWhitelistEntries(entries: List<String>) {
        stackTraceWhiteList.removeAll(entries)
        Timber.i("$TAG: Removing entries from " +
                "stack trace white list (if present): $entries\n" +
                "New stack trace white list = $stackTraceWhiteList" +
                if (!isStrictModeInitialized) "\n$WL_REMOVE_CLARIFICATION_MESSAGE" else "")
    }

    @JvmStatic
    @Synchronized
    fun wipeWhitelist() {
        Timber.i("$TAG: Wiping any present entries from " +
                "stack trace white list" +
                if (!isStrictModeInitialized) "\n$WL_REMOVE_CLARIFICATION_MESSAGE" else "")
        stackTraceWhiteList.clear()
    }

    private fun enableStrictMode() {
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

        Timber.i("$TAG: StrictMode Enabled")
    }

    private fun handleViolation(
        violation: Throwable, policyType: String, violationId: Int
    ): Boolean {
        val stackTrace = violation.stackTraceToString()

        val violationType = violation::class.simpleName
        Timber.d(
            "$TAG: Violation detected (ID: $violationId) " +
                    "in $policyType ($violationType). " +
                    "Checking against whitelisted strings..."
        )

        val matchedWhitelistEntry = stackTraceWhiteList.find { whitelist ->
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

            throw RuntimeException("Unhandled StrictMode $policyType violation " +
                    "(ID: $violationId)", violation)
        }
    }
}
