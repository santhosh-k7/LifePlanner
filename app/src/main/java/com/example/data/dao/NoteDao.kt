package com.example.data.dao

import androidx.room.*
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, isUrgent DESC, isImportant DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isUrgent = 1 AND isImportant = 1 ORDER BY isPinned DESC, updatedAt DESC")
    fun getUrgentImportantNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: Long): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :timestamp WHERE id = :id")
    suspend fun togglePin(id: Long, timestamp: Long = System.currentTimeMillis())
}
