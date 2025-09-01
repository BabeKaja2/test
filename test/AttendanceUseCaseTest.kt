package com.babetech.ucb_admin_access.test

import com.babetech.ucb_admin_access.data.AttendanceRepository
import com.babetech.ucb_admin_access.data.local.AttendanceWithStudent
import com.babetech.ucb_admin_access.domain.usecase.AttendanceFilter
import com.babetech.ucb_admin_access.domain.usecase.GetFilteredAttendanceUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AttendanceUseCaseTest {

    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var getFilteredAttendanceUseCase: GetFilteredAttendanceUseCase

    @Before
    fun setup() {
        attendanceRepository = mockk()
        getFilteredAttendanceUseCase = GetFilteredAttendanceUseCase(attendanceRepository)
    }

    @Test
    fun `execute should return filtered attendance records successfully`() = runTest {
        // Given
        val filter = AttendanceFilter(
            date = "2025-01-15",
            promotion = "L3",
            faculte = "Informatique"
        )
        
        val mockAttendanceList = listOf(
            AttendanceWithStudent(
                id = 1,
                studentMatricule = "UCB001",
                attendanceDate = "2025-01-15",
                attendanceTime = "08:30:00",
                timestamp = System.currentTimeMillis(),
                promotion = "L3",
                faculte = "Informatique",
                isPresent = true,
                studentData = """{"id":1,"matricule":"UCB001","fullname":"Jean Dupont","active":1,"schoolFilieres":{"id":1,"shortName":"INFO"},"schoolOrientations":{"id":1,"title":"Informatique"}}"""
            )
        )

        coEvery { 
            attendanceRepository.getFilteredAttendance("2025-01-15", "L3", "Informatique") 
        } returns mockAttendanceList

        // When
        val result = getFilteredAttendanceUseCase.execute(filter)

        // Then
        assertTrue("Result should be successful", result.isSuccess)
        val records = result.getOrNull()
        assertNotNull("Records should not be null", records)
        assertEquals("Should have 1 record", 1, records?.size)
        assertEquals("Student name should match", "Jean Dupont", records?.first()?.studentFullname)
        assertEquals("Promotion should match", "L3", records?.first()?.promotion)
    }

    @Test
    fun `execute should fail with invalid date format`() = runTest {
        // Given
        val filter = AttendanceFilter(date = "invalid-date")

        // When
        val result = getFilteredAttendanceUseCase.execute(filter)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertTrue(
            "Error message should mention date format",
            result.exceptionOrNull()?.message?.contains("Format de date invalide") == true
        )
    }

    @Test
    fun `getAvailablePromotions should return list of promotions`() = runTest {
        // Given
        val expectedPromotions = listOf("L1", "L2", "L3", "M1", "M2")
        coEvery { attendanceRepository.getAllPromotions() } returns expectedPromotions

        // When
        val result = getFilteredAttendanceUseCase.getAvailablePromotions()

        // Then
        assertEquals("Should return expected promotions", expectedPromotions, result)
    }

    @Test
    fun `getAvailableFacultes should return list of facultes`() = runTest {
        // Given
        val expectedFacultes = listOf("Informatique", "Gestion", "Droit")
        coEvery { attendanceRepository.getAllFacultes() } returns expectedFacultes

        // When
        val result = getFilteredAttendanceUseCase.getAvailableFacultes()

        // Then
        assertEquals("Should return expected facultes", expectedFacultes, result)
    }
}