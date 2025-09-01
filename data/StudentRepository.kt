package com.babetech.ucb_admin_access.data

import com.babetech.ucb_admin_access.api.ApiResponse

interface StudentRepository {
    suspend fun fetchStudentByMatricule(matricule: String): ApiResponse
    suspend fun getLastFetched(matricule: String): Long?
}
