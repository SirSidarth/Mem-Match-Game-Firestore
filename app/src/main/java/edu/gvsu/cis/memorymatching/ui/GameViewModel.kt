package edu.gvsu.cis.memorymatching.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val completed: Boolean
)

class GameViewModel : ViewModel() {

    var numCards = 16
    private var _numColumns = MutableStateFlow(4)
    val numColumnsFlow = _numColumns

    val gameCredits = "Ujjwal & Sidarth"

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
    private var firstTap = true
    private var timerJob: Job? = null

    var currentPlayer = ""

    // Matched colors for each card index
    private var _matchedColors = MutableStateFlow<MutableMap<Int, Color>>(mutableMapOf())
    val matchedColors = _matchedColors

    private lateinit var repository: GameStatsRepository

    fun initDatabase(context: Context) {
        val db = GameStatsDatabase.getDatabase(context)
        repository = GameStatsRepository(db.gameStatDao())
    }

    fun loadStatsFromDb() {
        viewModelScope.launch {
            repository.allStats.collect { dbStats ->
                _gameStats.value = dbStats.map {
                    GameStat(
                        playerName = it.playerName,
                        boardSize = it.boardSize,
                        numMoves = it.numMoves,
                        duration = it.duration,
                        completed = it.completed
                    )
                }
            }
        }
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
        firstTap = true
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
        val currentCards = _cards.value
        val currentFace = _faceUp.value

        if (currentFace[index]) return

        _faceUp.value = currentFace.mapIndexed { idx, face ->
            if (idx == index) true else face
        }.toMutableList()

        if (lastTappedIndex != null) {
            _moves.value += 1
            val firstIndex = lastTappedIndex!!
            val secondIndex = index

            if (currentCards[firstIndex] == currentCards[secondIndex]) {
                _matched.value = _matched.value.mapIndexed { idx, isMatched ->
                    if (idx == firstIndex || idx == secondIndex) true else isMatched
                }.toMutableList()

                val randomColor = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat()
                )
                _matchedColors.value[firstIndex] = randomColor
                _matchedColors.value[secondIndex] = randomColor

            } else {
                viewModelScope.launch {
                    delay(500)
                    _faceUp.value = _faceUp.value.mapIndexed { idx, face ->
                        if (idx == firstIndex || idx == secondIndex) false else face
                    }.toMutableList()
                }
            }

            lastTappedIndex = null
            firstTap = true
        } else {
            lastTappedIndex = index
            firstTap = false
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
                    completed = true
                )
            )
        }
    }

    fun restart() {
        addGameStat(
            GameStat(
                currentPlayer,
                "$numCards cards",
                _moves.value,
                _duration.value,
                completed = false
            )
        )
        startNewGame()
    }

    fun updateSettings(cards: Int, columns: Int) {
        numCards = cards
        _numColumns.value = columns
        startNewGame()
    }

    fun addGameStat(stat: GameStat) {
        _gameStats.update { it + stat }
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
    }

    fun sortStatsByMoves() { _gameStats.update { it.sortedBy { s -> s.numMoves } } }
    fun sortStatsByDuration() { _gameStats.update { it.sortedBy { s -> s.duration } } }
}