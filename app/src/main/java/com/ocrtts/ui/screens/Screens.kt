package com.ocrtts.ui.screens

import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(val route: String) {
    data object PermissionRequestScreen : Screens("permission")
    data object CameraScreen : Screens("camera")
    data object ImageScreen : Screens("image")
}