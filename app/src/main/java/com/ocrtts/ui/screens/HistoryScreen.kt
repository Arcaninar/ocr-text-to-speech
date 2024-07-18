package com.ocrtts.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    sharedViewModel: ImageSharedViewModel,
    dataStoreManager: DataStoreManager,
    modifier: Modifier = Modifier
) {
    val imageHistory by dataStoreManager.imageHistory.collectAsState(initial = emptySet())
    val sortedImageHistory = imageHistory.map { File(it) }.sortedByDescending { it.lastModified() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(sortedImageHistory) { file ->
            Log.d("HistoryScreen", "Processing file: ${file.absolutePath}, exists: ${file.exists()}")

            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            sharedViewModel.setImageInfo(bitmap)
                            sharedViewModel.updateFromHistory(true)
                            navController.navigate(Screens.ImageScreen.route)
                        }
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Text(
                        text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(file.lastModified()))
                    )
                }
            } else {
                Log.d("HistoryScreen", "File does not exist: ${file.absolutePath}")
            }
        }
    }
}