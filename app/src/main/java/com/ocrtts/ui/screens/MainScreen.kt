@file:OptIn(ExperimentalPermissionsApi::class)

package com.ocrtts.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ocrtts.ui.viewmodels.MainViewModel

//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun MainScreen() {
//    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
//    Log.i("check",cameraPermissionState.status.isGranted.toString())
//    MainContent(
//        hasPermission = cameraPermissionState.status.isGranted,
//        onRequestPermission = cameraPermissionState::launchPermissionRequest
//    )
//}
//
//@Composable
//private fun MainContent(
//    hasPermission: Boolean,
//    onRequestPermission: () -> Unit,
//    viewModel: MainViewModel = viewModel()
//) {
//    val navController = rememberNavController()
//    val startingScreen = if (hasPermission) Screens.CameraScreen else Screens.NoPermissionScreen
//
//    NavHost(navController = navController, startDestination = startingScreen.route) {
//        composable(Screens.NoPermissionScreen.route) {
//            NoPermissionScreen(onRequestPermission)
//        }
//
//        composable(Screens.CameraScreen.route) {
//            CameraScreen(viewModel) {
//                navController.navigate(Screens.ImageScreen.route)
//            }
//        }
//
//        composable(Screens.ImageScreen.route) {
//            ImageScreen(viewModel) {
//                navController.navigate(Screens.CameraScreen.route)
//            }
//        }
//    }
//}

//TODO
//Suggest Pass the whole navhost to each screen, but not a navigate function
private const val TAG="MainScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    Log.i(TAG,cameraPermissionState.status.isGranted.toString())
    val navController = rememberNavController()
    val startingScreen = if (cameraPermissionState.status.isGranted) Screens.CameraScreen else Screens.PermissionRequestScreen
    NavHost(navController = navController, startDestination = startingScreen.route) {
        composable(Screens.PermissionRequestScreen.route) {
            PermissionRequestScreen(navController=navController, cameraPermissionState=cameraPermissionState)
        }
        composable(Screens.CameraScreen.route) {
            CameraScreen(navController=navController,viewModel=viewModel)
        }
        composable(Screens.ImageScreen.route) {
            ImageScreen(viewModel) {
                navController.navigate(Screens.CameraScreen.route)
            }
        }
    }
    Log.i(TAG,"triggered")
}
//
//@Composable
//private fun MainContent(
//    hasPermission: Boolean,
//    onRequestPermission: () -> Unit,
//    viewModel: MainViewModel = viewModel()
//) {
//    val navController = rememberNavController()
//    val startingScreen = if (hasPermission) Screens.CameraScreen else Screens.NoPermissionScreen
//
//    NavHost(navController = navController, startDestination = startingScreen.route) {
//        composable(Screens.NoPermissionScreen.route) {
//            NoPermissionScreen(onRequestPermission)
//        }
//
//        composable(Screens.CameraScreen.route) {
//            CameraScreen(viewModel) {
//                navController.navigate(Screens.ImageScreen.route)
//            }
//        }
//
//        composable(Screens.ImageScreen.route) {
//            ImageScreen(viewModel) {
//                navController.navigate(Screens.CameraScreen.route)
//            }
//        }
//    }
//}

