package edu.gvsu.cis.memorymatching.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    viewModel: GameViewModel,
    playerName: String,
    onReset: () -> Unit,
    onNewPlayer: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateStats: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val faceUp by viewModel.faceUp.collectAsState()
    val matched by viewModel.matched.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val numColumns by viewModel.numColumnsFlow.collectAsState()
    val matchedColors by viewModel.matchedColors.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moves: $moves", fontSize = 18.sp)
                Text("Time: ${duration}s", fontSize = 18.sp)
                Row {
                    Button(onClick = onNavigateStats) { Text("Stats") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onNavigateSettings) { Text("Settings") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(numColumns),
                modifier = Modifier.weight(1f)
            ) {
                items(cards.size) { index ->
                    val cardValue = cards[index]
                    val isFace = faceUp.getOrNull(index) ?: false
                    val isMatchedCard = matched.getOrNull(index) ?: false
                    val cardColor = when {
                        isMatchedCard -> matchedColors[index] ?: Color.Red
                        isFace -> Color.Gray
                        else -> Color.LightGray
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .background(cardColor)
                            .clickable(enabled = !isMatchedCard) {
                                viewModel.tapCardAtIndex(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFace || isMatchedCard) {
                            Text(
                                text = cardValue?.toString() ?: "",
                                fontSize = 24.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Player: $playerName",
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onReset) { Text("Reset") }
                Button(onClick = onNewPlayer) { Text("New Player") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = viewModel.gameCredits,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Game Over!",
                        fontSize = 36.sp,
                        color = Color.Yellow,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onReset) { Text("Reset") }
                }
            }
        }
    }
}