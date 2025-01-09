package com.mitch.fontpicker.ui.screens.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.mitch.fontpicker.R
import timber.log.Timber

class PermissionsHandler(
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<String>,
    var onPermissionGranted: (String) -> Unit,
    var onPermissionDenied: (String) -> Unit,
    var onAllPermissionsGranted: () -> Unit // Callback for all permissions granted
) {
    companion object {
        const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        const val IMAGES_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES
        val SUPPORTED_PERMISSIONS = setOf(CAMERA_PERMISSION, IMAGES_PERMISSION)

        // List of permissions with UI descriptions (can be accessed by the UI layer)
        val permissionsToRequest = listOf(
            CAMERA_PERMISSION to R.string.access_camera,
            IMAGES_PERMISSION to R.string.access_images
        )
    }

    private var originalPolicy: StrictMode.ThreadPolicy? = null
    private val grantedPermissions = mutableSetOf<String>() // Tracks granted permissions

    /**
     * Request a single permission.
     */
    fun requestPermission(permission: String) {
        if (!SUPPORTED_PERMISSIONS.contains(permission)) {
            Timber.e("Unsupported permission requested: $permission")
            throw IllegalArgumentException("Unsupported permission: $permission")
        }

        if (isPermissionGranted(permission)) {
            Timber.i("$permission is already granted.")
            handlePermissionResult(permission, true)
        } else {
            Timber.i("Requesting permission: $permission")
            permissionLauncher.launch(permission)
        }
    }

    /**
     * Check if a permission is granted.
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
        val allGranted = SUPPORTED_PERMISSIONS.all { isPermissionGranted(it) }
        Timber.i("Checked if all permissions are granted: $allGranted")
        return allGranted
    }

    /**
     * Handle permission result.
     */
    fun handlePermissionResult(permission: String, isGranted: Boolean) {
        if (!SUPPORTED_PERMISSIONS.contains(permission)) {
            Timber.e("Permission result for unsupported permission: $permission")
            throw IllegalArgumentException("Unsupported permission: $permission")
        }

        if (isGranted) {
            grantedPermissions.add(permission)
            Timber.i("$permission granted. Current granted permissions: $grantedPermissions")

            onPermissionGranted(permission)

            // Check if all permissions are now granted
            if (grantedPermissions.containsAll(SUPPORTED_PERMISSIONS)) {
                Timber.i("All required permissions granted.")
                onAllPermissionsGranted()
            }
        } else {
            Timber.e("$permission denied.")
            onPermissionDenied(permission)
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
