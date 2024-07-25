package com.ocrtts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomIconButton(icon: ImageVector, description: String, modifier: Modifier = Modifier, color: Color = Color.White, size: Dp = 40.dp, innerPadding: Dp = 4.dp, onClick: () -> Unit) {
    val totalSize = size * 2
    IconButton(
        onClick = { onClick() },
        modifier = modifier
            .size(totalSize)
            .padding(totalSize / 10)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = color,
            modifier = Modifier
                .size(size)
                .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                .padding(innerPadding)
        )
    }
}