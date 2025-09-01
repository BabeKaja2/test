package com.babetech.ucb_admin_access.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val matricule: String,
    val payload: String,       // JSON complet de StudentData
    val lastFetched: Long      // millis depuis epoch
)
