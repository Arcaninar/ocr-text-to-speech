package com.ocrtts.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.ocrtts.ui.viewmodels.MainViewModel


@Composable
fun ImageScreen(viewModel: MainViewModel, modifier: Modifier = Modifier, navigate: () -> Unit) {
    val image = imageToBitmap(viewModel.imageSelected.value!!)
    
    Surface(modifier = modifier.background(Color.Transparent)) {
        Image(bitmap = image.asImageBitmap(), contentDescription = "Image", modifier = Modifier.fillMaxHeight())
        Button(onClick = { viewModel.setImageSelected(null)
            navigate() }, modifier = Modifier.size(40.dp, 40.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Back to video", modifier = Modifier.size(30.dp, 30.dp))
        }
    }
}

private fun imageToBitmap(image: Image): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer[bytes]
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}
