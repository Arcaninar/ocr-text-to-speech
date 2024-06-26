@file:OptIn(ExperimentalPermissionsApi::class)

package com.ocrtts.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ocrtts.ui.camera.CameraScreen
import com.ocrtts.ui.selected_image.ImageScreen
import com.ocrtts.ui.no_permission.NoPermissionScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {

    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    MainContent(
        hasPermission = cameraPermissionState.status.isGranted,
        onRequestPermission = cameraPermissionState::launchPermissionRequest
    )
}

@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: MainViewModel = viewModel<MainViewModel>()
) {
    val navController = rememberNavController()
    val startingScreen = if (hasPermission) Screens.CameraScreen else Screens.NoPermissionScreen

    NavHost(navController = navController, startDestination = startingScreen) {
        composable<Screens.NoPermissionScreen>{
            NoPermissionScreen(onRequestPermission)
        }

        composable<Screens.CameraScreen> {
            CameraScreen(viewModel) {
                navController.navigate(Screens.ImageScreen)
            }
        }

        composable<Screens.ImageScreen> {
            ImageScreen(viewModel) {
                navController.navigate(Screens.CameraScreen)
            }
        }
    }

//    if (hasPermission) {
//        CameraScreen()
//    } else {
//        NoPermissionScreen(onRequestPermission)
//    }
}
