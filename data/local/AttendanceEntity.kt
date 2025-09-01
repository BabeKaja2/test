package com.babetech.ucb_admin_access.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["matricule"],
            childColumns = ["studentMatricule"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentMatricule"]),
        Index(value = ["attendanceDate"]),
        Index(value = ["promotion"]),
        Index(value = ["faculte"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentMatricule: String,
    val attendanceDate: String, // Format: yyyy-MM-dd
    val attendanceTime: String, // Format: HH:mm:ss
    val timestamp: Long, // Timestamp de détection
    val promotion: String?, // Promotion de l'étudiant
    val faculte: String?, // Faculté de l'étudiant
    val isPresent: Boolean = true // Par défaut présent
)