package edu.gvsu.cis.memorymatching

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import edu.gvsu.cis.memorymatching.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val gameViewModel = remember { GameViewModel() }

            var currentScreen by remember { mutableStateOf("main") }
            var playerName by remember { mutableStateOf("") }
            var showNameDialog by remember { mutableStateOf(true) }

            // Initialize DB and load stats
            LaunchedEffect(Unit) {
                gameViewModel.initDatabase(this@MainActivity)
                gameViewModel.loadStatsFromDb()
            }

            MaterialTheme {
                Surface {
                    if (showNameDialog) {
                        Dialog(onDismissRequest = {}) {
                            var tempName by remember { mutableStateOf("") }
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 8.dp,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        "Enter Player Name:",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = tempName,
                                        onValueChange = { tempName = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            if (tempName.isNotBlank()) {
                                                playerName = tempName
                                                gameViewModel.currentPlayer = tempName
                                                showNameDialog = false
                                                gameViewModel.restart()
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Start")
                                    }
                                }
                            }
                        }
                    } else {
                        when (currentScreen) {
                            "main" -> MainScreen(
                                viewModel = gameViewModel,
                                playerName = playerName,
                                onReset = { gameViewModel.restart() },
                                onNewPlayer = {
                                    showNameDialog = true
                                },
                                onNavigateSettings = { currentScreen = "settings" },
                                onNavigateStats = { currentScreen = "stats" }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = gameViewModel,
                                onBack = { currentScreen = "main" }
                            )
                            "stats" -> StatsScreen(
                                viewModel = gameViewModel,
                                onBack = { currentScreen = "main" }
                            )
                        }
                    }
                }
            }
        }
    }
}