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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ui.screens.Screens
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

    // Group the files by date
    val groupedByDate = sortedImageHistory.groupBy { file ->
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(file.lastModified()))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Set background color to black
            .padding(16.dp)
    ) {
        groupedByDate.forEach { (date, files) ->
            Text(
                text = date,
                style = MaterialTheme.typography.subtitle1,
                color = Color.White, // Set text color to white for better contrast
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // Set the number of columns
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp), // Set content padding
                verticalArrangement = Arrangement.spacedBy(4.dp), // Set vertical spacing
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Set horizontal spacing
            ) {
                items(files) { file ->
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f) // Ensure square aspect ratio
                                .background(Color.Gray)
                                .clickable {
                                    sharedViewModel.setImageInfo(file.absolutePath, bitmap)
                                    navController.navigate(Screens.ImageScreen.route)
                                }
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize() // Fill the box
                            )
                        }
                    } else {
                        Log.d("HistoryScreen", "File does not exist: ${file.absolutePath}")
                    }
                }
            }
        }
    }
}

// Extension function to rotate a bitmap
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}