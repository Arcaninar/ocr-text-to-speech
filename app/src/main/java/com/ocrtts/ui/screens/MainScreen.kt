@file:OptIn(ExperimentalPermissionsApi::class)

package com.ocrtts.ui.screens

import HistoryScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import androidx.compose.ui.platform.LocalContext

//TODO
//Suggest Pass the whole navhost to each screen, but not a navigate function
private const val TAG="MainScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current     //添加network选择器的地方
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    Log.i(TAG,cameraPermissionState.status.isGranted.toString())
    val navController = rememberNavController()
    val startingScreen = if (cameraPermissionState.status.isGranted) Screens.HomeScreen else Screens.PermissionRequestScreen
    NavHost(navController = navController, startDestination = startingScreen.route) {
        composable(Screens.HomeScreen.route) { 
            HomeScreen(navController = navController)
        }
        composable(Screens.PermissionRequestScreen.route) {
            PermissionRequestScreen(navController = navController, cameraPermissionState = cameraPermissionState)
        }
        composable(Screens.TTSTestingScreen.route){
            TTSTestingScreen(navController = navController)
        }
        navigation(startDestination = Screens.CameraScreen.route, route = Screens.MainCameraScreen.route) {
            composable(Screens.CameraScreen.route) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                CameraScreen(navController = navController, sharedViewModel = sharedViewModel)
            }
            composable(
                route = "${Screens.ImageScreen.route}?fileName={fileName}",
                arguments = listOf(navArgument("fileName") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                val fileName = it.arguments?.getString("fileName")
                ImageScreen(fileName = fileName!!, sharedViewModel = sharedViewModel, navController = navController)
            }
            composable(Screens.HistoryScreen.route) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                HistoryScreen(navController = navController, sharedViewModel = sharedViewModel)
            }
        }
    }
    Log.i(TAG,"triggered")
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavHostController,
): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(parentEntry)
}