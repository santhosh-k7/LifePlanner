package com.example.data.dao

import androidx.room.*
import com.example.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {

    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE sessionType = 'POMODORO_FOCUS' OR sessionType = 'STOPWATCH'")
    fun getTotalFocusSeconds(): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE completedAt >= :startMillis AND (sessionType = 'POMODORO_FOCUS' OR sessionType = 'STOPWATCH')")
    fun getFocusSecondsSince(startMillis: Long): Flow<Long?>
}
