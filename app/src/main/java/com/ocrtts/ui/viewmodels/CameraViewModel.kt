package com.ocrtts.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.ocrtts.type.OCRText
import com.ocrtts.ui.screens.Screens
import com.ocrtts.utils.modifyBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG="MainViewModel"
class CameraViewModel : ViewModel() {
    private val _isRecognizedText = MutableStateFlow(false)
    val isRecognizedText = _isRecognizedText.asStateFlow()

    private val _hasTextBefore = MutableStateFlow(false)
    val hasTextBefore = _hasTextBefore.asStateFlow()

    fun updateRecognizedText(text: OCRText, isReset: Boolean) {
        if (isReset) {
            _isRecognizedText.value = false
        }
        _isRecognizedText.value = text.text.isNotBlank()
    }

    fun updateHasText(value: Boolean) {
        _hasTextBefore.value = value
    }

    fun captureImage(
        imageCapture: ImageCapture,
        context: Context,
        sharedViewModel: ImageSharedViewModel,
        navController: NavController
    ) {
        val TAG = "ImageCapture"
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val finalBitmap = modifyBitmap(image.toBitmap(), image.imageInfo.rotationDegrees, sharedViewModel.size)
                    sharedViewModel.setImageInfo(null.toString(),finalBitmap)
                    sharedViewModel.updateFromHistory(false)
                    val activity = context as Activity
                    val rotation: Int

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val display = activity.display
                        rotation = display?.rotation ?: 0
                    } else {
                        @Suppress("DEPRECATION")
                        val display = activity.windowManager.defaultDisplay
                        rotation = display.rotation
                    }

                    if (rotation == 1) { // Landscape left
                        sharedViewModel.updateOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    }
                    else if (rotation == 3) { // Landscape right
                        sharedViewModel.updateOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                    }
                    else { // Portrait
                        sharedViewModel.updateOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                    super.onCaptureSuccess(image)

                    navController.navigate(Screens.ImageScreen.route)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}")
                    super.onError(exception)
                }
            })
    }
}