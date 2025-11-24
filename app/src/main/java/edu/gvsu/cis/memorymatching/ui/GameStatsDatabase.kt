package edu.gvsu.cis.memorymatching.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entity: Represents a Game Stat
@Entity(tableName = "game_stats")
data class GameStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val boardSize: String,
    val numMoves: Int,
    val duration: Int,
    val completed: Boolean
)

// 2. DAO: Data Access Object
@Dao
interface GameStatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: GameStatEntity)

    @Query("SELECT * FROM game_stats ORDER BY id DESC")
    fun getAllStats(): Flow<List<GameStatEntity>>

    @Query("DELETE FROM game_stats")
    suspend fun clearAll()
}

// 3. Room Database
@Database(entities = [GameStatEntity::class], version = 1)
abstract class GameStatsDatabase : RoomDatabase() {
    abstract fun gameStatDao(): GameStatDao

    companion object {
        @Volatile
        private var INSTANCE: GameStatsDatabase? = null

        fun getDatabase(context: Context): GameStatsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameStatsDatabase::class.java,
                    "game_stats_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository for GameStats (optional, keeps ViewModel clean)
class GameStatsRepository(private val dao: GameStatDao) {

    val allStats: Flow<List<GameStatEntity>> = dao.getAllStats()

    suspend fun addStat(stat: GameStatEntity) {
        dao.insert(stat)
    }

    suspend fun clearStats() {
        dao.clearAll()
    }
}