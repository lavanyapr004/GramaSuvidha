package com.example.grama_suvidha

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromFeedbackList(value: List<Project.Feedback>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Project.Feedback>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toFeedbackList(value: String): List<Project.Feedback> {
        val gson = Gson()
        val type = object : TypeToken<List<Project.Feedback>>() {}.type
        return gson.fromJson(value, type)
    }
}
