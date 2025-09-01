package com.babetech.ucb_admin_access.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.coroutines.withTimeout

class ApiService(private val client: HttpClient) {

    suspend fun getStudentInfo(matricule: String): ApiResponse = withTimeout(10_000) {
        try {
            Log.i("ApiService", "Appel API pour le matricule : $matricule")

            val response = client
                .get("https://akhademie.ucbukavu.ac.cd/api/v1/school-students/read-by-matricule") {
                    url {
                        parameters.append("matricule", matricule)
                        Log.i("ApiService", "URL générée : $url")
                    }
                }

            Log.i("ApiService", "Réponse reçue avec status : ${response.status}")

            val body = response.body<ApiResponse>()
            Log.i("ApiService", "Données reçues : ${body.data}")

            body
        } catch (e: Exception) {
            Log.e("ApiService", "Erreur lors de l'appel API : ${e.message}", e)
            throw e
        }
    }
}

