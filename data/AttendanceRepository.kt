package com.babetech.ucb_admin_access.data

import com.babetech.ucb_admin_access.data.local.AttendanceWithStudent

interface AttendanceRepository {
    suspend fun recordAttendance(
        studentMatricule: String,
        promotion: String?,
        faculte: String?
    ): Result<Unit>
    
    suspend fun getAttendanceByDate(date: String): List<AttendanceWithStudent>
    
    suspend fun getFilteredAttendance(
        date: String,
        promotion: String? = null,
        faculte: String? = null
    ): List<AttendanceWithStudent>
    
    suspend fun getAllPromotions(): List<String>
    
    suspend fun getAllFacultes(): List<String>
    
    suspend fun isStudentAlreadyPresent(date: String, matricule: String): Boolean
}