package com.example.grama_suvidha

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes project data using LiveData.
 * This allows the UI to observe changes reactively
 * and update the RecyclerView automatically.
 */
class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    private val feedbackDao = AppDatabase.getDatabase(application).feedbackDao()

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>> = _projects

    private val _filteredProjects = MutableLiveData<List<Project>>()
    val filteredProjects: LiveData<List<Project>> = _filteredProjects

    private var allProjects: List<Project> = emptyList()
    private var currentSearch: String = ""
    private var currentCategory: String = "All"

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProjects()
    }

    private fun applyFilters() {
        var result = allProjects
        
        if (currentCategory != "All") {
            result = result.filter { it.category.contains(currentCategory, ignoreCase = true) }
        }
        
        if (currentSearch.isNotEmpty()) {
            result = result.filter { 
                it.nameEn.contains(currentSearch, ignoreCase = true) || 
                it.nameKn.contains(currentSearch, ignoreCase = true) ||
                it.locationEn.contains(currentSearch, ignoreCase = true) ||
                it.locationKn.contains(currentSearch, ignoreCase = true)
            }
        }
        _filteredProjects.value = result
    }

    fun filterBySearch(query: String) {
        currentSearch = query
        applyFilters()
    }

    fun filterByCategory(category: String) {
        currentCategory = category
        applyFilters()
    }

    fun getProjectFeedback(projectId: Int): LiveData<List<FeedbackEntry>> {
        return feedbackDao.getFeedbackByProject(projectId).asLiveData()
    }

    /**
     * Load projects from the local Room database.
     */
    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllProjects().collect { projectList ->
                allProjects = projectList
                _projects.value = projectList
                applyFilters()
                _isLoading.value = false
            }
        }
    }

    /**
     * Submit a rating to the database and save a feedback entry linked to the user.
     */
    fun submitFeedback(projectId: Int, rating: Float, userId: Int = 0, userName: String = "Anonymous", comment: String = "", imageUri: String? = null) {
        viewModelScope.launch {
            // Save the individual feedback entry
            val entry = FeedbackEntry(
                projectId = projectId,
                userId = userId,
                userName = userName,
                rating = rating,
                comment = comment,
                imageUri = imageUri
            )
            feedbackDao.insertFeedback(entry)

            // Update average rating on the project
            val avgRating = (feedbackDao.getAverageRating(projectId) ?: rating.toDouble()).toFloat()
            repository.updateProjectRating(projectId, avgRating)
        }
    }

    /**
     * Simulate progress increment — increases progress by 5%, capped at 100%.
     */
    fun simulateProgress(projectId: Int, currentProgress: Int) {
        viewModelScope.launch {
            val newProgress = (currentProgress + 5).coerceAtMost(100)
            repository.updateProjectProgress(projectId, newProgress)
        }
    }

    /**
     * Refresh projects - Room Flow usually handles this automatically, 
     * but we can re-trigger loading if needed.
     */
    fun refreshProjects() {
        loadProjects()
    }
}
