package com.mitch.fontpicker.ui.screens.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mitch.fontpicker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate")
class PermissionsHandler(
    val context: Context,
    private val activityResultRegistry: ActivityResultRegistry
) {
    companion object {
        const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        const val IMAGES_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES
        val SUPPORTED_PERMISSIONS = setOf(CAMERA_PERMISSION, IMAGES_PERMISSION)

        val permissionsToRequest = listOf(
            CAMERA_PERMISSION to R.string.access_camera,
            IMAGES_PERMISSION to R.string.access_images
        )
    }

    private var originalPolicy: StrictMode.ThreadPolicy? = null
    private val grantedPermissions = mutableSetOf<String>()
    private val permissionLaunchers = mutableMapOf<String, ActivityResultLauncher<String>>()

    private val _permissionResult = MutableStateFlow<Pair<String, Boolean>?>(null)
    val permissionResult: StateFlow<Pair<String, Boolean>?> = _permissionResult

    private val _allPermissionsGranted = MutableStateFlow(isPermissionGrantedForAll())
    val allPermissionsGranted: StateFlow<Boolean> = _allPermissionsGranted

    init {
        Timber.i("PermissionsHandler initialized.")
        registerPermissionLaunchers()
        updatePermissionsState() // Initialize with current state
    }

    /**
     * Dynamically register launchers for all supported permissions.
     */
    private fun registerPermissionLaunchers() {
        SUPPORTED_PERMISSIONS.forEach { permission ->
            val launcher = activityResultRegistry.register(
                "launcher_$permission",
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                handlePermissionResult(permission, isGranted)
            }
            permissionLaunchers[permission] = launcher
        }
        Timber.i("Permission launchers registered: $permissionLaunchers")
    }

    /**
     * Request a single permission using its corresponding launcher.
     */
    fun requestPermission(permission: String) {
        if (!SUPPORTED_PERMISSIONS.contains(permission)) {
            Timber.e("Unsupported permission requested: $permission")
            throw IllegalArgumentException("Unsupported permission: $permission")
        }

        relaxStrictMode()
        try {
            if (isPermissionGranted(permission)) {
                Timber.i("$permission is already granted. Skipping request.")
                handlePermissionResult(permission, true) // Emit already-granted state
            } else {
                Timber.i("Requesting permission: $permission")
                permissionLaunchers[permission]?.launch(permission)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during permission request for $permission")
            handlePermissionResult(permission, false) // Emit failure state
        }
    }

    /**
     * Check if a specific permission is granted.
     */
    fun isPermissionGranted(permission: String): Boolean {
        if (!SUPPORTED_PERMISSIONS.contains(permission)) {
            Timber.e("Unsupported permission checked: $permission")
            throw IllegalArgumentException("Unsupported permission: $permission")
        }

        val granted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        Timber.i("Permission check for $permission: ${if (granted) "GRANTED" else "DENIED"}")
        return granted
    }

    /**
     * Check if all permissions are granted.
     */
    fun isPermissionGrantedForAll(): Boolean {
        grantedPermissions.clear()
        SUPPORTED_PERMISSIONS.forEach { permission ->
            if (isPermissionGranted(permission)) {
                grantedPermissions.add(permission)
            }
        }

        val allGranted = grantedPermissions.containsAll(SUPPORTED_PERMISSIONS)
        Timber.i("All permissions granted: $allGranted")
        return allGranted
    }

    /**
     * Handle the result of a permission request.
     */
    private fun handlePermissionResult(permission: String, isGranted: Boolean) {
        if (!SUPPORTED_PERMISSIONS.contains(permission)) {
            Timber.e("Unsupported permission result: $permission")
            throw IllegalArgumentException("Unsupported permission: $permission")
        }

        _permissionResult.value = permission to isGranted

        if (isGranted) {
            grantedPermissions.add(permission)
            Timber.i("$permission granted. Current granted permissions: $grantedPermissions")
        } else {
            grantedPermissions.remove(permission)
            Timber.e("$permission denied.")
        }

        updatePermissionsState()
        restoreStrictMode()
    }

    /**
     * Update the state flows based on the current permissions.
     */
    private fun updatePermissionsState() {
        _allPermissionsGranted.update { isPermissionGrantedForAll() }
    }

    /**
     * Relax StrictMode policy.
     */
    fun relaxStrictMode() {
        originalPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(originalPolicy)
                .permitDiskReads()
                .permitDiskWrites()
                .build()
        )
        Timber.i("StrictMode relaxed.")
    }

    /**
     * Restore StrictMode policy.
     */
    fun restoreStrictMode() {
        originalPolicy?.let {
            StrictMode.setThreadPolicy(it)
            Timber.i("StrictMode restored to the original policy.")
        } ?: Timber.w("StrictMode restore called, but no original policy was saved!")
    }
}
