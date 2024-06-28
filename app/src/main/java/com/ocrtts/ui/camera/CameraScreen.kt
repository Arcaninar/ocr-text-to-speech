package com.ocrtts.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.text.Text
import com.ocrtts.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

data class TextRect(
    val text: String = "",
    var rect: Rect = Rect(0f, 0f, 0f, 0f)
)

@Composable
fun CameraScreen() {
    CameraContent()
}

@Composable
private fun CameraContent(viewModel: CameraScreenViewModel = viewModel()) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController = LifecycleCameraController(LocalContext.current)
    val audio = MediaPlayer.create(LocalContext.current, R.raw.ding)

    fun onTextUpdated(updatedText: Text, rotation: Int) {
        rotate(updatedText.textBlocks, rotation, viewModel::setTextRectList)
        if (updatedText.text.isNotBlank()) {
            if (!viewModel.previousHasText.value) {
                audio.start()
                viewModel.setPreviousHasText(true)
            }
        }
        else {
            viewModel.setPreviousHasText(false)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues: PaddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { Log.w("Test", "Test") },
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.hashCode())
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_START

                }.also { previewView ->
                    startTextRecognition(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        cameraController = cameraController,
                        previewView = previewView,
                        onDetectedTextUpdated = ::onTextUpdated,
                        viewModel = viewModel
                    )
                }
            }
        )

        val textRectSelected = viewModel.textRectSelected.value

        if (textRectSelected != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val box = textRectSelected.rect
                val path = Path().apply {
                    addRect(
                        rect = Rect(
                            left = box.left,
                            right = box.right,
                            top = box.top,
                            bottom = box.bottom
                        )
                    )
                }
                drawPath(path, color = Color.Red, style = Stroke(width = 5f))
            }
        }
    }
}


@SuppressLint("ClickableViewAccessibility")
private fun startTextRecognition(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraController: LifecycleCameraController,
    previewView: PreviewView,
    onDetectedTextUpdated: (Text, Int) -> Unit,
    viewModel: CameraScreenViewModel
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController

    previewView.isClickable = true

    // custom LongTouchListener
    previewView.setOnTouchListener { v, event ->
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val localCounter = viewModel.longTouchCounter.value

                for (text in viewModel.textRectList.value) {
                    if (contains(text.rect, event.x, event.y)) {
                        viewModel.setTextRectSelected(text)
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000L)
                    if (localCounter == viewModel.longTouchCounter.value) {
                    Log.w("Test", "Cords: " + event.x.toString() + ", " + event.y.toString())
                        for (text in viewModel.textRectList.value) {
                            Log.w("Test", "Box: (x | y)" + text.rect.left.toString() + ", " + text.rect.right.toString() + " | " + text.rect.top.toString() + ", " + text.rect.bottom.toString())
                            if (contains(text.rect, event.x, event.y)) {
                                // TODO: Text to Speech the text here
                                Log.w("Test", "the text: " + text.text)
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                viewModel.incrementLongTouch()
            }
        }

        v?.onTouchEvent(event) ?: true
    }
}

fun rotate(textBlocks: List<Text.TextBlock>, rotation: Int, updateRectTextList: (List<TextRect>) -> Unit) {
    Log.w("Rotation", rotation.toString())
    val updatedTextRects: MutableList<TextRect> = mutableListOf()

    when (rotation) {
        180 -> {
            for (text in textBlocks) {
                if (text.boundingBox != null) {
                    val textBlock = text.boundingBox!!
                    updatedTextRects.add(
                        TextRect(text.text, Rect(
                        top = textBlock.top.toFloat() * 2.25f,
                        bottom = textBlock.bottom.toFloat() * 2.325f,
                        right = textBlock.right.toFloat() * 2.3f,
                        left = textBlock.left.toFloat() * 2.1f
                    ))
                    )
                }
            }
        }
        270 -> {
            for (text in textBlocks) {
                if (text.boundingBox != null) {
                    val textBlock = text.boundingBox!!
                    updatedTextRects.add(
                        TextRect(text.text, Rect(
                        top = textBlock.top.toFloat() * 2.25f,
                        bottom = textBlock.bottom.toFloat() * 2.275f,
                        right = textBlock.right.toFloat() * 2.3f,
                        left = textBlock.left.toFloat() * 2.025f
                    ))
                    )
                }
            }
        }
        0 -> {
            for (text in textBlocks) {
                if (text.boundingBox != null) {
                    val textBlock = text.boundingBox!!
                    updatedTextRects.add(
                        TextRect(text.text, Rect(
                        top = textBlock.top.toFloat() * 2.225f,
                        bottom = textBlock.bottom.toFloat() * 2.275f,
                        right = textBlock.right.toFloat() * 2.3f,
                        left = textBlock.left.toFloat() * 2.2f
                    ))
                    )
                }
            }
        }
        else -> {
            for (text in textBlocks) {
                if (text.boundingBox != null) {
                    val textBlock = text.boundingBox!!
                    updatedTextRects.add(
                        TextRect(text.text, Rect(
                        top = textBlock.top.toFloat() * 2.2f,
                        bottom = textBlock.bottom.toFloat() * 2.25f,
                        right = textBlock.right.toFloat() * 2.25f,
                        left = textBlock.left.toFloat() * 1.85f
                    ))
                    )
                }
            }
        }
    }
    updateRectTextList(updatedTextRects)
}

private fun contains(rect: Rect, x: Float, y: Float): Boolean {
    return rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y
}
