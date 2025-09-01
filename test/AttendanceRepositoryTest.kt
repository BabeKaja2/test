package com.babetech.ucb_admin_access.test

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.babetech.ucb_admin_access.data.AttendanceRepositoryImpl
import com.babetech.ucb_admin_access.data.local.AppDatabase
import com.babetech.ucb_admin_access.data.local.AttendanceDao
import com.babetech.ucb_admin_access.data.local.StudentDao
import com.babetech.ucb_admin_access.data.local.StudentEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class AttendanceRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var attendanceDao: AttendanceDao
    private lateinit var studentDao: StudentDao
    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        attendanceDao = database.attendanceDao()
        studentDao = database.studentDao()
        repository = AttendanceRepositoryImpl(attendanceDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordAttendance_shouldInsertSuccessfully() = runTest {
        // Given
        val studentMatricule = "UCB001"
        val promotion = "L3"
        val faculte = "Informatique"

        // Insérer d'abord un étudiant
        val studentEntity = StudentEntity(
            matricule = studentMatricule,
            payload = """{"id":1,"matricule":"UCB001","fullname":"Jean Dupont","active":1}""",
            lastFetched = System.currentTimeMillis()
        )
        studentDao.upsert(studentEntity)

        // When
        val result = repository.recordAttendance(studentMatricule, promotion, faculte)

        // Then
        assertTrue("Should be successful", result.isSuccess)
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isPresent = repository.isStudentAlreadyPresent(today, studentMatricule)
        assertTrue("Student should be marked as present", isPresent)
    }

    @Test
    fun recordAttendance_shouldFailIfAlreadyPresent() = runTest {
        // Given
        val studentMatricule = "UCB001"
        val promotion = "L3"
        val faculte = "Informatique"

        // Insérer d'abord un étudiant
        val studentEntity = StudentEntity(
            matricule = studentMatricule,
            payload = """{"id":1,"matricule":"UCB001","fullname":"Jean Dupont","active":1}""",
            lastFetched = System.currentTimeMillis()
        )
        studentDao.upsert(studentEntity)

        // Enregistrer la première présence
        repository.recordAttendance(studentMatricule, promotion, faculte)

        // When - Tenter d'enregistrer à nouveau
        val result = repository.recordAttendance(studentMatricule, promotion, faculte)

        // Then
        assertTrue("Should fail", result.isFailure)
        assertTrue(
            "Error should mention already present",
            result.exceptionOrNull()?.message?.contains("déjà présent") == true
        )
    }

    @Test
    fun getFilteredAttendance_shouldReturnFilteredResults() = runTest {
        // Given
        val studentMatricule = "UCB001"
        val promotion = "L3"
        val faculte = "Informatique"

        // Insérer un étudiant et sa présence
        val studentEntity = StudentEntity(
            matricule = studentMatricule,
            payload = """{"id":1,"matricule":"UCB001","fullname":"Jean Dupont","active":1,"schoolFilieres":{"id":1,"shortName":"INFO"}}""",
            lastFetched = System.currentTimeMillis()
        )
        studentDao.upsert(studentEntity)
        repository.recordAttendance(studentMatricule, promotion, faculte)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // When
        val result = repository.getFilteredAttendance(today, promotion, faculte)

        // Then
        assertEquals("Should have 1 record", 1, result.size)
        assertEquals("Matricule should match", studentMatricule, result.first().studentMatricule)
        assertEquals("Promotion should match", promotion, result.first().promotion)
        assertEquals("Faculte should match", faculte, result.first().faculte)
    }
}