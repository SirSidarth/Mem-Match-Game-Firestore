package edu.gvsu.cis.memorymatching.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val stats = viewModel.gameStats.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TopBar(
            title = "Game Stats",
            showBackButton = true,
            onBackClick = { onBack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { viewModel.sortStatsByMoves() }) { Text("Sort by Moves") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { viewModel.sortStatsByDuration() }) { Text("Sort by Duration") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats) { stat ->
                Text(
                    text = "Player: ${stat.playerName} | Board: ${stat.boardSize} | Moves: ${stat.numMoves} | Time: ${stat.duration}s | Status: ${if (stat.completed) "Completed" else "Incomplete"}"
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}