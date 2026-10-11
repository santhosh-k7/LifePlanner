package com.example.data.dao

import androidx.room.*
import com.example.data.model.LeetCodeProblem
import kotlinx.coroutines.flow.Flow

@Dao
interface LeetCodeDao {

    @Query("SELECT * FROM leetcode_problems ORDER BY dateSolved DESC, id DESC")
    fun getAllProblems(): Flow<List<LeetCodeProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: LeetCodeProblem): Long

    @Update
    suspend fun updateProblem(problem: LeetCodeProblem)

    @Delete
    suspend fun deleteProblem(problem: LeetCodeProblem)

    @Query("DELETE FROM leetcode_problems WHERE id = :id")
    suspend fun deleteProblemById(id: Long)

    @Query("SELECT COUNT(*) FROM leetcode_problems WHERE difficulty = :diff")
    suspend fun countByDifficulty(diff: String): Int

    @Query("SELECT COUNT(DISTINCT dateSolved) FROM leetcode_problems")
    suspend fun countActiveDays(): Int
}
