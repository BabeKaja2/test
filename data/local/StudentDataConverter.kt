package com.babetech.ucb_admin_access.data.local


import androidx.room.TypeConverter
import com.babetech.ucb_admin_access.api.StudentData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StudentDataConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStudentData(data: StudentData): String =
        json.encodeToString(data)

    @TypeConverter
    fun toStudentData(raw: String): StudentData =
        json.decodeFromString(raw)
}
