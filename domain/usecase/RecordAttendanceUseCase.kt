package com.babetech.ucb_admin_access.domain.usecase

import android.util.Log
import com.babetech.ucb_admin_access.api.StudentData
import com.babetech.ucb_admin_access.data.AttendanceRepository

class RecordAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    
    companion object {
        private const val TAG = "RecordAttendanceUseCase"
    }

    suspend fun execute(studentData: StudentData): Result<Unit> {
        return try {
            Log.i(TAG, "Enregistrement de la présence pour ${studentData.matricule}")
            
            // Validation des données étudiant
            if (studentData.active != 1) {
                return Result.failure(IllegalStateException("L'étudiant n'est pas actif"))
            }

            // Extraction des informations de promotion et faculté
            val promotion = extractPromotion(studentData)
            val faculte = extractFaculte(studentData)

            attendanceRepository.recordAttendance(
                studentMatricule = studentData.matricule,
                promotion = promotion,
                faculte = faculte
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'enregistrement de la présence", e)
            Result.failure(e)
        }
    }

    private fun extractPromotion(studentData: StudentData): String? {
        // Logique pour extraire la promotion depuis les données étudiant
        // Peut être basée sur schoolFilieres ou schoolOrientations
        return studentData.schoolFilieres?.shortName
    }

    private fun extractFaculte(studentData: StudentData): String? {
        // Logique pour extraire la faculté depuis les données étudiant
        // Peut être basée sur schoolOrientations ou d'autres champs
        return studentData.schoolOrientations?.title
    }
}