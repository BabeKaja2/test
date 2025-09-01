package com.babetech.ucb_admin_access.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babetech.ucb_admin_access.api.StudentData
import com.babetech.ucb_admin_access.data.local.StudentDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.babetech.ucb_admin_access.data.local.StudentEntity

// Modèle combiné pour l'UI
data class StudentCached(
    val matricule: String,
    val fullname: String,
    val schoolFilieres: String?,
    val schoolOrientations: String?,
    val lastFetched: Long
)

class RapportViewModel(
    private val dao: StudentDao
) : ViewModel() {

    private val _students = dao.getAllEntities()
        .map { entities ->
            entities.map { ent ->
                // Décoder le payload JSON en StudentData
                val sd = Json.decodeFromString(com.babetech.ucb_admin_access.api.StudentData.serializer(), ent.payload)
                StudentCached(
                    matricule = sd.matricule,
                    fullname = sd.fullname,
                    schoolFilieres = sd.schoolFilieres?.shortName,
                    schoolOrientations = sd.schoolOrientations?.title,
                    lastFetched = ent.lastFetched
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Flow des étudiants cachés
     */
    val students: StateFlow<List<StudentCached>> = _students

    /**
     * Permet de forcer le rechargement depuis la base
     */
    fun refresh() {
        viewModelScope.launch {
            // Rien de spécial ici : getAllEntities est un Flow auto-update
        }
    }
}
