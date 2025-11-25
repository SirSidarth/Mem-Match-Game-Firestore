package edu.gvsu.cis.memorymatching.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.graphics.Color
import edu.gvsu.cis.memorymatching.data.*

data class GameStat(
    val playerName: String,
    val boardSize: String,
    val numMoves: Int,
    val duration: Int,
    val completed: Boolean,
    val source: String
)

class GameViewModel : ViewModel() {

    enum class StatsSaveLocation { ROOM, FIRESTORE }

    var statsSaveLocation: StatsSaveLocation = StatsSaveLocation.ROOM
    val gameCredits = "Ujjwal & Sidarth"

    private val firestore = FirebaseFirestore.getInstance()

    var numCards = 16
    private var _numColumns = MutableStateFlow(4)
    val numColumnsFlow = _numColumns

    private var _cards = MutableStateFlow<List<Int>>(emptyList())
    val cards = _cards

    private var _faceUp = MutableStateFlow<MutableList<Boolean>>(mutableListOf())
    val faceUp = _faceUp

    private var _matched = MutableStateFlow<MutableList<Boolean>>(mutableListOf())
    val matched = _matched

    private var _moves = MutableStateFlow(0)
    val moves = _moves

    private var _gameStats = MutableStateFlow<List<GameStat>>(emptyList())
    val gameStats = _gameStats

    private var _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver

    private var _duration = MutableStateFlow(0)
    val duration = _duration

    private var lastTappedIndex: Int? = null
    private var timerJob: Job? = null

    var currentPlayer = ""

    private var _matchedColors = MutableStateFlow<MutableMap<Int, Color>>(mutableMapOf())
    val matchedColors = _matchedColors

    private lateinit var repository: GameStatsRepository

    fun initDatabase(context: Context) {
        val db = GameStatsDatabase.getDatabase(context)
        repository = GameStatsRepository(db.gameStatDao())
    }

    fun loadStats() {
        loadRoomStats()
        loadFirestoreStats()
    }

    private fun loadRoomStats() {
        viewModelScope.launch {
            repository.allStats.collect { dbStats ->
                val roomStats = dbStats.map {
                    GameStat(
                        playerName = it.playerName,
                        boardSize = it.boardSize,
                        numMoves = it.numMoves,
                        duration = it.duration,
                        completed = it.completed,
                        source = "ROOM"
                    )
                }
                mergeStats(roomStats)
            }
        }
    }

    private fun loadFirestoreStats() {
        firestore.collection("game_stats")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val fireStats = snapshot.documents.mapNotNull { doc ->
                    GameStat(
                        playerName = doc.getString("playerName") ?: return@mapNotNull null,
                        boardSize = doc.getString("boardSize") ?: "",
                        numMoves = doc.getLong("numMoves")?.toInt() ?: 0,
                        duration = doc.getLong("duration")?.toInt() ?: 0,
                        completed = doc.getBoolean("completed") ?: false,
                        source = "FIRESTORE"
                    )
                }
                mergeStats(fireStats)
            }
    }

    private fun mergeStats(newList: List<GameStat>) {
        val existing = _gameStats.value.toMutableList()
        existing.removeAll { old ->
            newList.any {
                it.playerName == old.playerName &&
                        it.numMoves == old.numMoves &&
                        it.duration == old.duration &&
                        it.source == old.source
            }
        }
        _gameStats.value = existing + newList
    }

    init { startNewGame() }

    fun startNewGame() {
        val numbers = (1..(numCards / 2)).toList()
        val shuffled = (numbers + numbers).shuffled(Random(System.currentTimeMillis()))
        _cards.value = shuffled
        _faceUp.value = MutableList(numCards) { false }
        _matched.value = MutableList(numCards) { false }
        _moves.value = 0
        _duration.value = 0
        lastTappedIndex = null
        _isGameOver.value = false
        _matchedColors.value.clear()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_isGameOver.value) {
                delay(1000)
                _duration.value += 1
            }
        }
    }

    fun tapCardAtIndex(index: Int) {
        val faces = _faceUp.value
        if (faces[index]) return

        _faceUp.value = faces.mapIndexed { idx, f -> if (idx == index) true else f }.toMutableList()

        if (lastTappedIndex != null) {
            _moves.value += 1
            val first = lastTappedIndex!!
            val second = index

            if (_cards.value[first] == _cards.value[second]) {

                val randomColor = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat()
                )

                _matched.value = _matched.value.mapIndexed { idx, m ->
                    if (idx == first || idx == second) true else m
                }.toMutableList()

                _matchedColors.value[first] = randomColor
                _matchedColors.value[second] = randomColor

            } else {
                viewModelScope.launch {
                    delay(600)
                    _faceUp.value = _faceUp.value.mapIndexed { idx, f ->
                        if (idx == first || idx == second) false else f
                    }.toMutableList()
                }
            }

            lastTappedIndex = null
        } else {
            lastTappedIndex = index
        }

        if (_matched.value.all { it }) {
            _isGameOver.value = true
            timerJob?.cancel()

            addGameStat(
                GameStat(
                    playerName = currentPlayer,
                    boardSize = "$numCards cards",
                    numMoves = _moves.value,
                    duration = _duration.value,
                    completed = true,
                    source = "LOCAL"
                )
            )
        }
    }

    fun restart() {
        addGameStat(
            GameStat(
                playerName = currentPlayer,
                boardSize = "$numCards cards",
                numMoves = _moves.value,
                duration = _duration.value,
                completed = false,
                source = "LOCAL"
            )
        )
        startNewGame()
    }

    fun updateSettings(cards: Int, columns: Int) {
        numCards = cards
        _numColumns.value = columns
        startNewGame()
    }

    private fun saveToFirestore(stat: GameStat) {
        val map = hashMapOf(
            "playerName" to stat.playerName,
            "boardSize" to stat.boardSize,
            "numMoves" to stat.numMoves,
            "duration" to stat.duration,
            "completed" to stat.completed
        )
        firestore.collection("game_stats").add(map)
    }

    fun addGameStat(stat: GameStat) {
        if (statsSaveLocation == StatsSaveLocation.ROOM) {
            viewModelScope.launch {
                repository.addStat(
                    GameStatEntity(
                        playerName = stat.playerName,
                        boardSize = stat.boardSize,
                        numMoves = stat.numMoves,
                        duration = stat.duration,
                        completed = stat.completed
                    )
                )
            }
        } else {
            saveToFirestore(stat)
        }
    }

    fun setSaveLocation(location: StatsSaveLocation) {
        statsSaveLocation = location
    }

    fun sortStatsByMoves() {
        _gameStats.update { it.sortedBy { s -> s.numMoves } }
    }

    fun sortStatsByDuration() {
        _gameStats.update { it.sortedBy { s -> s.duration } }
    }
}