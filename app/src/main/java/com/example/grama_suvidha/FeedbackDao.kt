package com.example.grama_suvidha

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FeedbackDao {
    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntry): Long

    @Query("SELECT * FROM feedback_entries WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getFeedbackByProject(projectId: Int): kotlinx.coroutines.flow.Flow<List<FeedbackEntry>>

    @Query("SELECT AVG(rating) FROM feedback_entries WHERE projectId = :projectId")
    suspend fun getAverageRating(projectId: Int): Double?

    @Query("SELECT COUNT(*) FROM feedback_entries WHERE userId = :userId")
    suspend fun getFeedbackCountByUser(userId: Int): Long

    @Query("SELECT COUNT(*) FROM feedback_entries")
    suspend fun getTotalFeedbackCount(): Long
}
