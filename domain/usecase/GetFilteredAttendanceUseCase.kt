package com.babetech.ucb_admin_access.domain.usecase

import android.util.Log
import com.babetech.ucb_admin_access.api.StudentData
import com.babetech.ucb_admin_access.data.AttendanceRepository
import com.babetech.ucb_admin_access.data.local.AttendanceWithStudent
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceFilter(
    val date: String,
    val promotion: String? = null,
    val faculte: String? = null
)

data class AttendanceRecord(
    val id: Long,
    val studentMatricule: String,
    val studentFullname: String,
    val attendanceDate: String,
    val attendanceTime: String,
    val timestamp: Long,
    val promotion: String?,
    val faculte: String?,
    val isPresent: Boolean,
    val studentFiliere: String?,
    val studentOrientation: String?
)

class GetFilteredAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    
    companion object {
        private const val TAG = "GetFilteredAttendanceUseCase"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    suspend fun execute(filter: AttendanceFilter): Result<List<AttendanceRecord>> {
        return try {
            Log.i(TAG, "Récupération des présences avec filtre: $filter")
            
            // Validation de la date
            if (!isValidDate(filter.date)) {
                return Result.failure(IllegalArgumentException("Format de date invalide. Utilisez yyyy-MM-dd"))
            }

            val attendanceList = attendanceRepository.getFilteredAttendance(
                date = filter.date,
                promotion = filter.promotion,
                faculte = filter.faculte
            )

            val records = attendanceList.map { attendance ->
                val studentData = parseStudentData(attendance.studentData)
                AttendanceRecord(
                    id = attendance.id,
                    studentMatricule = attendance.studentMatricule,
                    studentFullname = studentData?.fullname ?: "Nom inconnu",
                    attendanceDate = attendance.attendanceDate,
                    attendanceTime = attendance.attendanceTime,
                    timestamp = attendance.timestamp,
                    promotion = attendance.promotion,
                    faculte = attendance.faculte,
                    isPresent = attendance.isPresent,
                    studentFiliere = studentData?.schoolFilieres?.shortName,
                    studentOrientation = studentData?.schoolOrientations?.title
                )
            }

            Log.i(TAG, "Trouvé ${records.size} enregistrements de présence")
            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des présences filtrées", e)
            Result.failure(e)
        }
    }

    suspend fun getAvailablePromotions(): List<String> {
        return attendanceRepository.getAllPromotions()
    }

    suspend fun getAvailableFacultes(): List<String> {
        return attendanceRepository.getAllFacultes()
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            dateFormat.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseStudentData(jsonPayload: String): StudentData? {
        return try {
            Json.decodeFromString(StudentData.serializer(), jsonPayload)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing des données étudiant", e)
            null
        }
    }
}