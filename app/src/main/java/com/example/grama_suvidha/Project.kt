package com.example.grama_suvidha

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: Int = 0,
    val nameEn: String = "",
    val nameKn: String = "",
    val descriptionEn: String = "",
    val descriptionKn: String = "",
    val locationEn: String = "",
    val locationKn: String = "",
    val statusEn: String = "",
    val statusKn: String = "",
    val progress: Int = 0,
    val budget: String = "",
    val expectedCompletion: String = "",
    val imageUrlBefore: String? = null,
    val imageUrlAfter: String? = null,
    val rating: Float = 0f,
    val latitude: Double = 12.9716, // Default to Bangalore area
    val longitude: Double = 77.5946,
    val category: String = "Infrastructure",
    val totalBudget: Long = 0L,
    val fundsReleased: Long = 0L,
    val feedbacks: List<Feedback> = emptyList()
) : Serializable {

    data class Feedback(
        val user: String = "",
        val comment: String = "",
        val rating: Float = 0f
    ) : Serializable

    val name: String
        get() = if (java.util.Locale.getDefault().language == "kn") nameKn else nameEn

    val description: String
        get() = if (java.util.Locale.getDefault().language == "kn") descriptionKn else descriptionEn

    val status: String
        get() = if (java.util.Locale.getDefault().language == "kn") statusKn else statusEn

    val location: String
        get() = if (java.util.Locale.getDefault().language == "kn") locationKn else locationEn
}
