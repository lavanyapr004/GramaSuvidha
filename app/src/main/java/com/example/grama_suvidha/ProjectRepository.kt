package com.example.grama_suvidha

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ProjectRepository(private val context: Context) {

    private val projectDao = AppDatabase.getDatabase(context).projectDao()

    private suspend fun ensureDatabasePopulated() {
        withContext(Dispatchers.IO) {
            try {
                if (projectDao.getCount() == 0L) {
                    val projects = loadProjectsFromAssets()
                    if (projects.isNotEmpty()) {
                        projectDao.insertAll(projects)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProjectsFromAssets(): List<Project> {
        return try {
            val jsonString = context.assets.open("projects.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<Project>>() {}.type
            com.google.gson.Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Returns a Flow of all projects from the database.
     * Ensures data is loaded from assets if empty.
     */
    suspend fun getAllProjects(): Flow<List<Project>> {
        ensureDatabasePopulated()
        return projectDao.getAllProjects()
    }

    suspend fun updateProjectProgress(projectId: Int, progress: Int) {
        withContext(Dispatchers.IO) {
            projectDao.updateProgress(projectId, progress)
        }
    }

    suspend fun updateProjectRating(projectId: Int, rating: Float) {
        withContext(Dispatchers.IO) {
            projectDao.updateRating(projectId, rating)
        }
    }

    /**
     * Returns a specific project by ID.
     * Ensures data is loaded from assets if empty.
     */
    suspend fun getProjectById(id: Int): Project? {
        ensureDatabasePopulated()
        return withContext(Dispatchers.IO) {
            projectDao.getProjectByIdOnce(id)
        }
    }
}
