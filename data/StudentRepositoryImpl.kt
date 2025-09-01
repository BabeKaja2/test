package com.babetech.ucb_admin_access.data

import com.babetech.ucb_admin_access.api.ApiResponse
import com.babetech.ucb_admin_access.api.ApiService
import com.babetech.ucb_admin_access.api.StudentData
import com.babetech.ucb_admin_access.data.local.StudentDao
import com.babetech.ucb_admin_access.data.local.StudentEntity
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

class StudentRepositoryImpl(
    private val apiService: ApiService,
    private val dao: StudentDao
) : StudentRepository {

    override suspend fun fetchStudentByMatricule(matricule: String): ApiResponse {
        // 1) Cherche en cache local
        dao.getByMatricule(matricule)?.let { ent ->
            val data = Json.decodeFromString(StudentData.serializer(), ent.payload)
            return ApiResponse(message = "FromCache", data = data, errors = null)
        }

        // 2) Sinon appel réseau
        val response = withTimeout(10_000L) {
            apiService.getStudentInfo(matricule)
        }

        // 3) Enregistre en cache local si OK
        response.data?.let { sd ->
            val raw = Json.encodeToString(StudentData.serializer(), sd)
            val now = System.currentTimeMillis()
            dao.upsert(
                StudentEntity(
                    matricule = sd.matricule,
                    payload = raw,
                    lastFetched = now
                )
            )
        }

        // 4) On renvoie la réponse réseau
        return response
    }

    /**
     * Retourne le timestamp (millis) de la dernière mise à jour locale pour ce matricule.
     */
    override suspend fun getLastFetched(matricule: String): Long? {
        return dao.getByMatricule(matricule)?.lastFetched
    }
}
