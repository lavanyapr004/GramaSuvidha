package com.example.grama_suvidha

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Int): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<Project>): List<Long>

    @Query("UPDATE projects SET progress = :progress WHERE id = :projectId")
    suspend fun updateProgress(projectId: Int, progress: Int): Int

    @Query("UPDATE projects SET rating = :rating WHERE id = :projectId")
    suspend fun updateRating(projectId: Int, rating: Float): Int
    
    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getCount(): Long

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdOnce(id: Int): Project?
}
