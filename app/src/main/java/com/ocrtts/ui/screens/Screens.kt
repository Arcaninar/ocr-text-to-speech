package com.ocrtts.ui.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(val route: String) {
    data object HomeScreen : Screens("home")
    data object PermissionRequestScreen : Screens("permission")
    data object MainCameraScreen : Screens("main_camera")
    data object CameraScreen : Screens("camera")
    data object ImageScreen : Screens("image")
    data object HistoryScreen : Screens("history")
    data object AlbumScreen : Screens("album")
    data object TTSTestingScreen : Screens("testing")
}