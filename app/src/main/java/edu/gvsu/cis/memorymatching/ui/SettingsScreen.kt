package edu.gvsu.cis.memorymatching.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val numColumns by viewModel.numColumnsFlow.collectAsState()
    val numCards by remember { mutableStateOf(viewModel.numCards) }

    var tempColumns by remember { mutableStateOf(numColumns) }
    var tempCards by remember { mutableStateOf(numCards) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        TopBar(
            title = "Settings",
            showBackButton = true,
            onBackClick = { onBack() }
        )

        Column {
            Text(text = "Number of Columns: $tempColumns")
            Slider(
                value = tempColumns.toFloat(),
                onValueChange = { tempColumns = it.toInt() },
                valueRange = 2f..8f,
                steps = 6
            )
        }

        Column {
            Text(text = "Number of Cards: $tempCards")
            Slider(
                value = tempCards.toFloat(),
                onValueChange = {
                    val evenValue = (it.toInt() / 2) * 2
                    tempCards = evenValue.coerceIn(4, 64)
                },
                valueRange = 4f..64f,
                steps = 30
            )
        }

        Button(onClick = { viewModel.updateSettings(tempCards, tempColumns) }) {
            Text("Confirm")
        }

        Button(onClick = { onBack() }) { Text("Back") }
    }
}