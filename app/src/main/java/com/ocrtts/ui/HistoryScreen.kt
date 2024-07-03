package com.ocrtts.ui.history

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("photo_history", Context.MODE_PRIVATE)
    val allEntries = sharedPreferences.all.entries.sortedByDescending { it.key.toLong() } // Sort by descending to show latest first
    val limitedEntries = remember { allEntries.take(10) } // Limit to 10 entries

    // Log the entries to debug
    Log.d("HistoryScreen", "All entries: $allEntries")
    Log.d("HistoryScreen", "Limited entries: $limitedEntries")

    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(limitedEntries) { entry ->
            val fileName = entry.value as String
            val file = File(fileName) // Use the absolute path directly
            Log.d("HistoryScreen", "Processing file: ${file.absolutePath}, exists: ${file.exists()}")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        // Handle image click to reload the image
                        navController.navigate("imageScreen?fileName=${file.absolutePath}")
                    }) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().padding(8.dp))
                    Text(text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(entry.key.toLong())))
                }
            } else {
                Log.d("HistoryScreen", "File does not exist: ${file.absolutePath}")
            }

        }
    }
}