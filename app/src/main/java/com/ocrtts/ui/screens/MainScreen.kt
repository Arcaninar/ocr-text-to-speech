@file:OptIn(ExperimentalPermissionsApi::class)

package com.ocrtts.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ocrtts.ui.viewmodels.MainViewModel

//TODO
//Suggest Pass the whole navhost to each screen, but not a navigate function
private const val TAG="MainScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    Log.i(TAG,cameraPermissionState.status.isGranted.toString())
    val navController = rememberNavController()
    var startingScreen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cameraPermissionState.status) {
        Log.i(TAG, "Camera permission status: ${cameraPermissionState.status.isGranted}")
        if (startingScreen == null) {
            startingScreen = if (cameraPermissionState.status.isGranted) {
                Screens.CameraScreen.route
            } else {
                Screens.PermissionRequestScreen.route
            }
        } else if (cameraPermissionState.status.isGranted &&
            navController.currentDestination?.route == Screens.PermissionRequestScreen.route) {
            navController.navigate(Screens.CameraScreen.route) {
                popUpTo(Screens.PermissionRequestScreen.route) { inclusive = true }
            }
        }
    }

    if(startingScreen!=null){
        NavHost(navController = navController, startDestination = startingScreen!!) {
            composable(Screens.PermissionRequestScreen.route) {
                PermissionRequestScreen( cameraPermissionState=cameraPermissionState)
            }
            composable(Screens.CameraScreen.route) {
                CameraScreen(navController=navController,viewModel=viewModel)
            }
            composable(Screens.ImageScreen.route) {
//            ImageScreen(viewModel) {
//                navController.navigate(Screens.CameraScreen.route)
//            }
            }
        }
    }

    Log.i(TAG,"triggered")
}

