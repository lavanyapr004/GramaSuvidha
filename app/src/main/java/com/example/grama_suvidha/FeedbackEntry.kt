package com.example.grama_suvidha

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback_entries")
data class FeedbackEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int = 0,
    val userId: Int = 0,
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
