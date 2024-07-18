package com.ocrtts.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ocr.OfflineOCR
import com.ocrtts.ocr.OnlineOCR
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.SettingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO
//Suggest Pass the whole navhost to each screen, but not a navigate function
private const val TAG="MainScreen"


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(settingViewModel: SettingViewModel) {
    //添加network选择器的地方

    // initialize OnlineOCR and OfflineOCR objects so when they analyze the text, the OCR is already initialized and save some time
    LaunchedEffect(key1 = true) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.i("InitializeOCR", "InitializeOCR")
            OnlineOCR
            OfflineOCR
        }
    }

    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    Log.i(TAG,cameraPermissionState.status.isGranted.toString())
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
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
        composable(Screens.SettingScreen.route){
            SettingScreen(navController = navController, settingViewModel = settingViewModel)
        }
        navigation(startDestination = Screens.CameraScreen.route, route = Screens.MainCameraScreen.route) {
            composable(Screens.CameraScreen.route) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                CameraScreen(navController = navController, sharedViewModel = sharedViewModel)
            }
            composable(Screens.ImageScreen.route) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                ImageScreen(sharedViewModel = sharedViewModel, navController = navController, dataStoreManager = dataStoreManager)
            }
            composable(Screens.HistoryScreen.route) {
                val sharedViewModel = it.sharedViewModel<ImageSharedViewModel>(navController)
                HistoryScreen(navController = navController, sharedViewModel = sharedViewModel, dataStoreManager = dataStoreManager)
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

