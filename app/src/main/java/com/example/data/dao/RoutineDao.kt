package com.example.data.dao

import androidx.room.*
import com.example.data.model.Routine
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Transaction
    @Query("SELECT * FROM routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<RoutineWithSteps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: RoutineStep): Long

    @Update
    suspend fun updateStep(step: RoutineStep)

    @Delete
    suspend fun deleteStep(step: RoutineStep)

    @Query("UPDATE routine_steps SET completedDate = :date WHERE id = :stepId")
    suspend fun markStepCompleted(stepId: Long, date: String?)

    @Query("UPDATE routine_steps SET completedDate = NULL WHERE routineId = :routineId")
    suspend fun resetRoutineStepsForDay(routineId: Long)
}
