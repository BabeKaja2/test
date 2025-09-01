package com.babetech.ucb_admin_access.data

import android.util.Log
import com.babetech.ucb_admin_access.data.local.AttendanceDao
import com.babetech.ucb_admin_access.data.local.AttendanceEntity
import com.babetech.ucb_admin_access.data.local.AttendanceWithStudent
import java.text.SimpleDateFormat
import java.util.*

class AttendanceRepositoryImpl(
    private val attendanceDao: AttendanceDao
) : AttendanceRepository {

    companion object {
        private const val TAG = "AttendanceRepository"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    override suspend fun recordAttendance(
        studentMatricule: String,
        promotion: String?,
        faculte: String?
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val currentDate = dateFormat.format(Date(now))
            val currentTime = timeFormat.format(Date(now))

            // Vérifier si l'étudiant est déjà présent aujourd'hui
            val alreadyPresent = isStudentAlreadyPresent(currentDate, studentMatricule)
            if (alreadyPresent) {
                Log.i(TAG, "Étudiant $studentMatricule déjà présent aujourd'hui")
                return Result.failure(Exception("Étudiant déjà présent aujourd'hui"))
            }

            val attendance = AttendanceEntity(
                studentMatricule = studentMatricule,
                attendanceDate = currentDate,
                attendanceTime = currentTime,
                timestamp = now,
                promotion = promotion,
                faculte = faculte,
                isPresent = true
            )

            attendanceDao.insertAttendance(attendance)
            Log.i(TAG, "Présence enregistrée pour $studentMatricule à $currentTime")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'enregistrement de la présence", e)
            Result.failure(e)
        }
    }

    override suspend fun getAttendanceByDate(date: String): List<AttendanceWithStudent> {
        return try {
            attendanceDao.getAttendanceByDate(date)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des présences pour $date", e)
            emptyList()
        }
    }

    override suspend fun getFilteredAttendance(
        date: String,
        promotion: String?,
        faculte: String?
    ): List<AttendanceWithStudent> {
        return try {
            attendanceDao.getFilteredAttendance(date, promotion, faculte)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération filtrée des présences", e)
            emptyList()
        }
    }

    override suspend fun getAllPromotions(): List<String> {
        return try {
            attendanceDao.getAllPromotions()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des promotions", e)
            emptyList()
        }
    }

    override suspend fun getAllFacultes(): List<String> {
        return try {
            attendanceDao.getAllFacultes()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des facultés", e)
            emptyList()
        }
    }

    override suspend fun isStudentAlreadyPresent(date: String, matricule: String): Boolean {
        return try {
            attendanceDao.getAttendanceCountForStudentOnDate(date, matricule) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification de présence", e)
            false
        }
    }
}