package com.babetech.ucb_admin_access.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)
    
    @Query("""
        SELECT a.*, s.payload as studentData 
        FROM attendance a 
        INNER JOIN students s ON a.studentMatricule = s.matricule 
        WHERE a.attendanceDate = :date
        ORDER BY a.attendanceTime DESC
    """)
    suspend fun getAttendanceByDate(date: String): List<AttendanceWithStudent>
    
    @Query("""
        SELECT a.*, s.payload as studentData 
        FROM attendance a 
        INNER JOIN students s ON a.studentMatricule = s.matricule 
        WHERE a.attendanceDate = :date 
        AND (:promotion IS NULL OR a.promotion = :promotion)
        AND (:faculte IS NULL OR a.faculte = :faculte)
        ORDER BY a.attendanceTime DESC
    """)
    suspend fun getFilteredAttendance(
        date: String,
        promotion: String? = null,
        faculte: String? = null
    ): List<AttendanceWithStudent>
    
    @Query("""
        SELECT DISTINCT promotion 
        FROM attendance 
        WHERE promotion IS NOT NULL 
        ORDER BY promotion
    """)
    suspend fun getAllPromotions(): List<String>
    
    @Query("""
        SELECT DISTINCT faculte 
        FROM attendance 
        WHERE faculte IS NOT NULL 
        ORDER BY faculte
    """)
    suspend fun getAllFacultes(): List<String>
    
    @Query("""
        SELECT COUNT(*) 
        FROM attendance 
        WHERE attendanceDate = :date 
        AND studentMatricule = :matricule
    """)
    suspend fun getAttendanceCountForStudentOnDate(date: String, matricule: String): Int
    
    @Query("DELETE FROM attendance WHERE attendanceDate = :date")
    suspend fun deleteAttendanceByDate(date: String)
    
    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()
    
    @Query("""
        SELECT a.*, s.payload as studentData 
        FROM attendance a 
        INNER JOIN students s ON a.studentMatricule = s.matricule 
        ORDER BY a.timestamp DESC
    """)
    fun getAllAttendanceFlow(): Flow<List<AttendanceWithStudent>>
}

// Classe de données pour joindre attendance et student
data class AttendanceWithStudent(
    val id: Long,
    val studentMatricule: String,
    val attendanceDate: String,
    val attendanceTime: String,
    val timestamp: Long,
    val promotion: String?,
    val faculte: String?,
    val isPresent: Boolean,
    val studentData: String // JSON payload du student
)