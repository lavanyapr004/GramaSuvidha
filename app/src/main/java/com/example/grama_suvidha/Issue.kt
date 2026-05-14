package com.example.grama_suvidha

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int = 0,
    val userId: Int = 0,
    val subject: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "",
    val attachmentUri: String? = null,
    val projectName: String = "",
    val status: String = "Open",
    val createdAt: Long = System.currentTimeMillis()
)
