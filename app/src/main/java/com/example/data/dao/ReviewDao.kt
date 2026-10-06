package com.example.data.dao

import androidx.room.*
import com.example.data.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews ORDER BY date DESC, id DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Query("SELECT * FROM reviews WHERE reviewType = :type ORDER BY date DESC LIMIT 1")
    suspend fun getLatestReview(type: String): Review?
}
