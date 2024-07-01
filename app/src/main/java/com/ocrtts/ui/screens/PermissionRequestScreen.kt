package com.ocrtts.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ocrtts.ui.viewmodels.MAX_DENY_COUNT
import com.ocrtts.ui.viewmodels.PermissionViewModel
import com.ocrtts.ui.viewmodels.ViewModelFactory

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
    navController: NavController,
    cameraPermissionState: PermissionState,
    modifier: Modifier = Modifier,
    viewModel: PermissionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    var alert by remember { mutableStateOf("") }
    var btnText by remember { mutableStateOf("") }
    var btnAction by remember { mutableStateOf<() -> Unit>({}) }


    LaunchedEffect(viewModel.denyCount) {
        Log.i(TAG,"lauchedeffect denyCount")
        if (viewModel.denyCount > MAX_DENY_COUNT) {
            alert = "Camera permission required for this feature to be available. Please grant the permission in Settings manually."
            btnText = "Go to Settings"
            btnAction = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
        else{
            alert = "The camera is important for this app. Please grant the permission."
            btnText="Request permission"
            btnAction = {
                cameraPermissionState.launchPermissionRequest()
                viewModel.incrementDenyCount()
            }
        }
    }
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        Log.i(TAG, "lauchedeffect cameraPermissionState.status")
        if (cameraPermissionState.status.isGranted) {
            viewModel.resetDenyCount()
            navController.navigate(Screens.CameraScreen.route) {
                popUpTo(Screens.PermissionRequestScreen.route) { inclusive = true }
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


//@Composable
//fun NoPermissionContent(
//    onRequestPermission: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
//        ) {
//            Text(
//                textAlign = TextAlign.Center,
//                text = "Please grant the permission to use the camera to use the core functionality of this app."
//            )
//            Button(onClick = onRequestPermission) {
//                Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
//                Text(text = "Grant permission")
//            }
//        }
//    }
//}

//@Preview
//@Composable
//private fun Preview_NoPermissionContent() {
//    NoPermissionContent(
//        onRequestPermission = {}
//    )
//}


