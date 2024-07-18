import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
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
                            bitmap = loadThumbnail(file)
                        }

                        bitmap?.let { croppedBitmap ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(Color.Gray)
                                    .clickable {
                                        sharedViewModel.setImageInfo(file.absolutePath, BitmapFactory.decodeFile(file.absolutePath))
                                        navController.navigate(Screens.ImageScreen.route)
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

// Safely loads a thumbnail image
suspend fun loadThumbnail(file: File): Bitmap? = withContext(Dispatchers.IO) {
    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
    originalBitmap?.let {
        val croppedBitmap = it.cropToSquare()
        Bitmap.createScaledBitmap(croppedBitmap, 100, 100, true)
    }
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