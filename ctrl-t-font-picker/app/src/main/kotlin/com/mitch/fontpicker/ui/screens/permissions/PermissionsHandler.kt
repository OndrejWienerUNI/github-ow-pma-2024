package com.mitch.fontpicker.ui.screens.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mitch.fontpicker.R
import timber.log.Timber

@Suppress("MemberVisibilityCanBePrivate")
class PermissionsHandler(
    private val context: Context,
    private val activityResultRegistry: ActivityResultRegistry,
    var onPermissionGranted: (String) -> Unit,
    var onPermissionDenied: (String) -> Unit,
    var onAllPermissionsGranted: () -> Unit
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

    init {
        Timber.i("PermissionsHandler initialized.")
        registerPermissionLaunchers()
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

        if (isPermissionGranted(permission)) {
            Timber.i("$permission is already granted. Triggering callback.")
            onPermissionGranted(permission)
        } else {
            Timber.i("Requesting permission: $permission")
            permissionLaunchers[permission]?.launch(permission)
                ?: Timber.e("Launcher for $permission not found!")
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
        val grantedPermissions = SUPPORTED_PERMISSIONS.filter { isPermissionGranted(it) }
        val deniedPermissions = SUPPORTED_PERMISSIONS - grantedPermissions.toSet()

        Timber.i("Granted permissions: $grantedPermissions")
        Timber.i("Denied permissions: $deniedPermissions")

        val allGranted = deniedPermissions.isEmpty()
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

        if (isGranted) {
            grantedPermissions.add(permission)
            Timber.i("$permission granted. Current granted permissions: $grantedPermissions")
            onPermissionGranted(permission)
        } else {
            Timber.e("$permission denied.")
            onPermissionDenied(permission)
        }

        // Check if all permissions are granted after the result.
        if (grantedPermissions.containsAll(SUPPORTED_PERMISSIONS)) {
            Timber.i("All required permissions granted.")
            onAllPermissionsGranted()
        }
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
