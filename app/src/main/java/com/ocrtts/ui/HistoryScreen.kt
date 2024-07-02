package com.ocrtts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ocrtts.R

@Composable
fun HistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(navController.currentBackStackEntryAsState().value!!)
) {
    val imageHistory = viewModel.getImageHistory()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(imageHistory) { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "History Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color.Gray)
                )
            }
        }

//        IconButton(
//            onClick = {
//                navController.navigate("homeScreen")
//            },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_home),
//                contentDescription = "Home Icon",
//                tint = Color.Black,
//                modifier = Modifier.size(30.dp)
//            )
//        }
    }
}