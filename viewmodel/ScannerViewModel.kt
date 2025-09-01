package com.babetech.ucb_admin_access.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babetech.ucb_admin_access.api.StudentData
import com.babetech.ucb_admin_access.ble.BleScanner
import com.babetech.ucb_admin_access.data.StudentRepository
import com.babetech.ucb_admin_access.domain.usecase.RecordAttendanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class ScannerViewModel(
    private val bleScanner: BleScanner,
    private val repository: StudentRepository,
    private val recordAttendanceUseCase: RecordAttendanceUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ScannerViewModel"
        private const val TIMEOUT_DURATION_MS = 10 * 60 * 1000L // 10 minutes
    }

    private val _studentInfo = MutableStateFlow<StudentData?>(null)
    val studentInfo: StateFlow<StudentData?> = _studentInfo

    private val _lastFetched = MutableStateFlow<Long?>(null)
    val lastFetched: StateFlow<Long?> = _lastFetched

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Cache pour éviter les appels répétés
    private val detectedMatricules = mutableMapOf<String, Long>()

    init {
        bleScanner.onMatriculeDetected = { matricule ->
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                val lastDetectedTime = detectedMatricules[matricule]

                // Vérifie le délai de 10 minutes
                if (lastDetectedTime != null && currentTime - lastDetectedTime < TIMEOUT_DURATION_MS) {
                    Log.i(TAG, "Matricule $matricule ignoré (scanné il y a ${currentTime - lastDetectedTime} ms)")
                    return@launch
                }

                // Met à jour le cache
                detectedMatricules[matricule] = currentTime

                Log.i(TAG, "Matricule reçu : $matricule - début traitement")

                try {
                    // Récupération locale/remote
                    val response = repository.fetchStudentByMatricule(matricule)
                    Log.i(TAG, "Réponse API reçue : ${'$'}{response.data}")

                    response.data?.let { sd ->
                        _studentInfo.value = sd
                        // Récupère la date de dernier fetch depuis le repository
                        val fetchedTime = repository.getLastFetched(sd.matricule)
                        _lastFetched.value = fetchedTime
                        Log.i(TAG, "Dernier fetch pour ${'$'}{sd.matricule} = $fetchedTime")

                        if (sd.active == 1) {
                            Log.i(TAG, "Étudiant actif : mise à jour de studentInfo")
                            
                            // Enregistrer la présence automatiquement
                            recordAttendanceUseCase.execute(sd)
                                .onSuccess {
                                    Log.i(TAG, "Présence enregistrée avec succès pour ${sd.matricule}")
                                }
                                .onFailure { error ->
                                    Log.w(TAG, "Erreur lors de l'enregistrement de la présence: ${error.message}")
                                    // On ne bloque pas l'affichage même si l'enregistrement échoue
                                }
                        } else {
                            _errorMessage.value = "Étudiant inactif ou non trouvé."
                            Log.w(TAG, "Étudiant inactif ou non trouvé pour matricule ${'$'}{sd.matricule}")
                        }
                    } ?: run {
                        _errorMessage.value = "Aucune donnée reçue pour ce matricule."
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur API : ${'$'}{e.localizedMessage}'", e)
                    _errorMessage.value = "Erreur de connexion ou de lecture des données."
                }
            }
        }
    }

    fun startScanning() {
        Log.i(TAG, "Démarrage du scan BLE")
        bleScanner.startScanning()
    }

    fun stopScanning() {
        Log.i(TAG, "Arrêt du scan BLE")
        bleScanner.stopScanning()
    }

    fun clearError() {
        Log.i(TAG, "Effacement du message d'erreur")
        _errorMessage.value = null
    }
}
