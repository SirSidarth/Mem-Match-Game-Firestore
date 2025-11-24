package edu.gvsu.cis.memorymatching.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

@Composable
fun MemoryCell(modifier: Modifier = Modifier, key: Int?, isFaceUp: Boolean?) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, Color.Blue)
            .background(
                when (isFaceUp) {
                    null -> Color.Gray
                    true -> Color.White
                    false -> Color.LightGray
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp == true && key != null) {
            Text(text = "$key", fontSize = 28.sp)
        }
    }
}