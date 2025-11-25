package edu.gvsu.cis.memorymatching.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
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
            Button(onClick = { viewModel.sortStatsByDuration() }) { Text("Sort by Time") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats) { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Player: ${stat.playerName}")
                        Text("Board: ${stat.boardSize}")
                        Text("Moves: ${stat.numMoves} | Time: ${stat.duration}s")
                        Text("Status: ${if (stat.completed) "Completed" else "Incomplete"}")
                    }

                    if (stat.source == "FIRESTORE") {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Cloud",
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Local",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Divider()
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