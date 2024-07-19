package com.ocrtts.ui.viewmodels

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
        config: Configuration,
        sharedViewModel: ImageSharedViewModel,
        navController: NavController
    ) {
        val TAG = "ImageCapture"
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val finalBitmap = modifyBitmap(image.toBitmap(), image.imageInfo.rotationDegrees, sharedViewModel.size)
                    sharedViewModel.setImageInfo(finalBitmap)
                    sharedViewModel.updateFromHistory(false)
                    Log.i("orientation", config.orientation.toString())
                    if (config.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        sharedViewModel.updateOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                    else {
                        sharedViewModel.updateOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
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