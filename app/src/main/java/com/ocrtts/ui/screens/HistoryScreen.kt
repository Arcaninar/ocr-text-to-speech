import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ui.screens.Screens
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // Group the files by date
    val groupedByDate = sortedImageHistory.groupBy { file ->
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(file.lastModified()))
    }

    var selectedImages by remember { mutableStateOf(setOf<File>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        if (selectedImages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected ${selectedImages.size} images",
                    color = Color.White,
                    style = MaterialTheme.typography.subtitle1
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedImages.forEach { file ->
                                file.delete()
                            }
                            selectedImages = emptySet()
                            dataStoreManager.updateImageHistory()
                            isSelectionMode = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.size(100.dp, 48.dp) // Adjust the size as needed
                ) {
                    Text("Delete")
                }
            }
        }

        groupedByDate.forEach { (date, files) ->
            Text(
                text = date,
                style = MaterialTheme.typography.subtitle1,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(files) { file ->
                    if (file.exists()) {
                        var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                        LaunchedEffect(file) {
                            bitmap = loadImage(file)
                        }

                        bitmap?.let { croppedBitmap ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedImages.contains(file)) Color.White else Color.Transparent
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                isSelectionMode = true
                                                selectedImages = selectedImages + file
                                            },
                                            onTap = {
                                                if (isSelectionMode) {
                                                    if (selectedImages.contains(file)) {
                                                        selectedImages = selectedImages - file
                                                        if (selectedImages.isEmpty()) {
                                                            isSelectionMode = false
                                                        }
                                                    } else {
                                                        selectedImages = selectedImages + file
                                                    }
                                                } else {
                                                    sharedViewModel.setImageInfo(file.absolutePath, BitmapFactory.decodeFile(file.absolutePath))
                                                    navController.navigate(Screens.ImageScreen.route)
                                                }
                                            }
                                        )
                                    }
                            ) {
                                Image(
                                    bitmap = croppedBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        Log.d("HistoryScreen", "File does not exist: ${file.absolutePath}")
                    }
                }
            }
        }
    }
}

// Safely loads and crops a thumbnail image
suspend fun loadImage(file: File): Bitmap? = withContext(Dispatchers.IO) {
    BitmapFactory.decodeFile(file.absolutePath)?.cropToSquare()
}

// Extension function to crop a bitmap to square
fun Bitmap.cropToSquare(): Bitmap {
    return if (width >= height) {
        Bitmap.createBitmap(this, (width - height) / 2, 0, height, height)
    } else {
        Bitmap.createBitmap(this, 0, (height - width) / 2, width, width)
    }
}

// Extension function to rotate a bitmap
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}