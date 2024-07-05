package com.ocrtts.ui.screens

import PermissionViewModel
import ViewModelFactory
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import android.provider.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

//TODO
//LaunchedEffect for Compose event
//Reject two times -> Goto Setting
//Shared Preference

//Use `res/values/strings.xml` to perform Localization
//<string name="grant_permission">Grant permission</string>
//Text(text = stringResource(id = R.string.grant_permission))

private const val TAG="PermissionRequestScreen"


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    cameraPermissionState: PermissionState,
    modifier: Modifier = Modifier,
    viewModel: PermissionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val denyCount by viewModel.denyCount.collectAsState()

    LaunchedEffect(cameraPermissionState.status) {
        Log.i(TAG,"Update Status")
        viewModel.updateCameraPermissionStatus(cameraPermissionState.status.isGranted)
        if (cameraPermissionState.status.isGranted) {
            Log.i(TAG, "Camera permission granted, resetting deny count")
            viewModel.resetDenyCount()
        }

    }

    val (alert, btnText, btnAction) = remember(denyCount) {
        if (denyCount > PermissionViewModel.MAX_DENY_COUNT) {
            Triple(
                "Camera permission required for this feature to be available. Please grant the permission in Settings manually.",
                "Go to Settings"
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        } else {
            Triple(
                "The camera is important for this app. Please grant the permission.",
                "Request permission"
            ) {
                cameraPermissionState.launchPermissionRequest()
                viewModel.incrementDenyCount()
            }
        }
    }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(alert, textAlign = TextAlign.Center)
                Button(
                    onClick = btnAction,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = null)
                    Text(btnText)
                }
            }

}

