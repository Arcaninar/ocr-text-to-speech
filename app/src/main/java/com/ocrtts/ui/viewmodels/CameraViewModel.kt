package com.ocrtts.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.type.OCRText
import com.ocrtts.ui.screens.Screens
import com.ocrtts.utils.modifyBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

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
//
//class RotationVectorSensorProvider(context: Context) : SensorEventListener2 {
//    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val accelerometerReading = FloatArray(3)
//    private val magnetometerReading = FloatArray(3)
//
//    private val rotationMatrix = FloatArray(9)
//    private val orientationAngles = FloatArray(3)
//
//    init {
//        startListening()
//    }
//
//    fun startListening() {
////        rotationVectorSensor?.let { sensor ->
////            sensorManager.registerListener(
////                this,
////                sensor,
////                SensorManager.SENSOR_DELAY_UI
////            )
////        }
//
//        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
//            sensorManager.registerListener(
//                this,
//                accelerometer,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
//            sensorManager.registerListener(
//                this,
//                magneticField,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//    }
//
//    fun stopListening() {
//        sensorManager.unregisterListener(this)
//    }
//
//    fun updateOrientationAngles() {
//        // Update rotation matrix, which is needed to update orientation angles.
//        SensorManager.getRotationMatrix(
//            rotationMatrix,
//            null,
//            accelerometerReading,
//            magnetometerReading
//        )
//
//        // "rotationMatrix" now has up-to-date information.
//
//        SensorManager.getOrientation(rotationMatrix, orientationAngles)
//
//        // "orientationAngles" now has up-to-date information.
////        Log.i("rotation", (orientationAngles[0] * 180 / 3.14).toString())
//        Log.i("rotation", "" + (orientationAngles[1] + orientationAngles[2]) * 90 / 3.14)
////        Log.i("rotation", "" + (orientationAngles[0] + orientationAngles[1] + orientationAngles[2]) / 3 * 180 / 3.14)
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        // Not used
//    }
//
//    override fun onFlushCompleted(sensor: Sensor?) {
//        // Not used
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
////        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
////            rotationValues = event.values
////            val test = event.values
//////            Log.i("rotation", "0: " + test[0].toString())
//////            Log.i("rotation", "1: " + test[1].toString())
//////            Log.i("rotation", "2: " + test[2].toString())
//////            val p = atan(test[0]/sqrt(test[1].pow(2) + test[2].pow(2)))
//////            val onta = atan(test[2]/sqrt(test[1].pow(2) + test[0].pow(2)))
//////            val theta = atan(sqrt(test[0].pow(2) + test[2].pow(2))/test[1])
//////            Log.i("rotation", p.toString())
////            val ax = test[0]
////            val ay = test[1]
////            val az = test[2]
////            val pitch = atan(ax / sqrt((ay.pow(2) + az.pow(2))))
////            val roll = atan(ay / sqrt((ax.pow(2) + az.pow(2))))
////            val theta = atan(sqrt((ax.pow(2) + ay.pow(2))) / az)
////            Log.i("rotation", (roll * 180 / 3.14).toString())
////        }
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
//        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
//        }
//        updateOrientationAngles()
//    }
//}

//class RotationVectorSensorProvider(context: Context) : SensorEventListener2 {
//    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    private val accelerometerReading = FloatArray(3)
//    private val magnetometerReading = FloatArray(3)
//
//    private val rotationMatrix = FloatArray(9)
//    private val orientationAngles = FloatArray(3)
//
//    init {
//        startListening()
//    }
//
//    fun startListening() {
//        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.also { accelerometer ->
//            sensorManager.registerListener(
//                this,
//                accelerometer,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//    }
//
//    fun stopListening() {
//        sensorManager.unregisterListener(this)
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
////            rotationValues = event.values
//            val ax = event.values[0]
//            val ay = event.values[1]
//            val az = event.values[2]
//
//            val x = sqrt(ax.pow(2) + ay.pow(2))
//            val y = sqrt(ax.pow(2) + az.pow(2))
//            val z = sqrt(ay.pow(2) + az.pow(2))
//            Log.i("rotation", y.toString())
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        //
//    }
//
//    override fun onFlushCompleted(sensor: Sensor?) {
//        //
//    }
//}