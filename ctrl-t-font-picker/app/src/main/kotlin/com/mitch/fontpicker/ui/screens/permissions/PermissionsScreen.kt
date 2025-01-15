package com.mitch.fontpicker.ui.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import timber.log.Timber

private val SCREEN_PADDING = 24.dp
private val TEXT_BUTTON_SPACING = 24.dp

@Composable
fun PermissionsRoute(
    viewModel: PermissionsViewModel
) {
    Timber.d("Rendering PermissionsRoute")

    val currentPermissionIndex by viewModel.currentPermissionIndex.collectAsState()
    val allPermissionsGranted by viewModel.allPermissionsGranted.collectAsState()

    Timber.d(
        "PermissionsRoute: Current Permission Index = $currentPermissionIndex, " +
                "All Permissions Granted = $allPermissionsGranted"
    )

    PermissionsScreen(
        currentPermissionIndex = currentPermissionIndex,
        allPermissionsGranted = allPermissionsGranted,
        onRequestPermission = {
            Timber.d("Request permission button clicked")
            viewModel.requestPermission()
        },
    )
}

@Composable
fun PermissionsScreen(
    currentPermissionIndex: Int,
    allPermissionsGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!allPermissionsGranted) {
        PermissionsScreenContent(
            messageResId = PermissionsHandler.permissionsToRequest[currentPermissionIndex].second,
            onRequestPermission = onRequestPermission,
            modifier = modifier
        )
    } else {
        Timber.i("All permissions granted. Navigation or other actions can be triggered here.")
        // Add actions for granted permissions if needed
    }
}

@Composable
fun PermissionsScreenContent(
    messageResId: Int,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SCREEN_PADDING)
        ) {
            Text(
                text = stringResource(id = messageResId),
                style = FontPickerDesignSystem.typography.titleMedium,
                color = FontPickerDesignSystem.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(TEXT_BUTTON_SPACING))

            Button(
                onClick = {
                    Timber.d("Grant Permission button clicked")
                    onRequestPermission()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FontPickerDesignSystem.colorScheme.primaryContainer,
                    contentColor = FontPickerDesignSystem.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(id = R.string.grant_permission))
            }
        }
    }
}

@Preview(name = "PermissionsScreenContentPreview")
@Composable
fun PermissionsScreenContentPreview() {
    FontPickerTheme {
        PermissionsScreenContent(
            messageResId = R.string.grant_permission, // Replace with real or mock string resource
            onRequestPermission = { /* Preview action */ }
        )
    }
}
