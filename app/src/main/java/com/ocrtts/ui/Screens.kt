package com.ocrtts.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class Screens {
    @Serializable
    data object NoPermissionScreen: Screens()

    @Serializable
    data object CameraScreen: Screens()

    @Serializable
    data object ImageScreen: Screens()
}