package com.example.grama_suvidha

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val panchayatName: String = "Hosahalli Grama Panchayat",
    val joinedDate: String = "",
    val avatarResId: Int = 0
)
