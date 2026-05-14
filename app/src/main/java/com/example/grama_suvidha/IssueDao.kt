package com.example.grama_suvidha

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IssueDao {
    @Insert
    suspend fun insertIssue(issue: Issue): Long

    @Query("SELECT * FROM issues WHERE projectId = :projectId ORDER BY createdAt DESC")
    suspend fun getIssuesByProject(projectId: Int): List<Issue>

    @Query("SELECT * FROM issues WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getIssuesByUser(userId: Int): List<Issue>

    @Query("SELECT COUNT(*) FROM issues WHERE userId = :userId")
    suspend fun getIssueCountByUser(userId: Int): Long

    @Query("SELECT COUNT(*) FROM issues")
    suspend fun getTotalIssueCount(): Long
}
