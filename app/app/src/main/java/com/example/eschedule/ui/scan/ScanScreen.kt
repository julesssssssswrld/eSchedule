package com.example.eschedule.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eschedule.theme.UepBlue

/**
 * Scan screen placeholder — camera pipeline to be implemented separately.
 * Displays a full-screen dark background with centered placeholder text.
 */
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Camera feed goes here",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}
