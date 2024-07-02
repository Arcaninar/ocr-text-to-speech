package com.ocrtts.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.text.Text
import com.ocrtts.R
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ocrtts.ui.MainViewModel

data class TextRect(
    val text: String = "",
    var rect: Rect = Rect(0f, 0f, 0f, 0f)
)

@Composable
fun CameraScreen(viewModel: MainViewModel, modifier: Modifier = Modifier, navigate: () -> Unit) {
    var currentContext = LocalContext.current
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
        modifier = modifier.fillMaxSize(),
    ) { paddingValues: PaddingValues ->
        Box(contentAlignment = Alignment.BottomEnd) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable { Log.w("Test", "Test") },
                factory = { context ->
                    currentContext = context
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

            if (viewModel.textRectList.value.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .offset(y = (-20).dp)
                    .fillMaxWidth()) {
                    Text(text = "There is a text in front of you. Click the button below to view it",
                        modifier = Modifier
                            .background(Color.Yellow, RoundedCornerShape(50))
                            .padding(5.dp)
                            .fillMaxWidth(0.8f)
                    )
                    Button(
                        onClick = {
                            onClickButton(context = currentContext, cameraController = cameraController, viewModel, navigate)
                        }) {
                        CircleShape
                    }

                }
            }
        }

    }
}

private fun onClickButton(context: Context, cameraController: LifecycleCameraController, viewModel: MainViewModel, navigate: () -> Unit) {
    cameraController.takePicture(
        ContextCompat.getMainExecutor(context),
        object: ImageCapture.OnImageCapturedCallback() {
            @OptIn(ExperimentalGetImage::class)
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                if (image.image != null) {
                    viewModel.setImageSelected(image.image)
                    navigate()
                }
            }
        }
    )
}


@SuppressLint("ClickableViewAccessibility")
private fun startTextRecognition(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraController: LifecycleCameraController,
    previewView: PreviewView,
    onDetectedTextUpdated: (Text, Int) -> Unit,
    viewModel: MainViewModel
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController

    previewView.isClickable = true

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
