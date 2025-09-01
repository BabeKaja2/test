package com.babetech.ucb_admin_access.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val route: String,         // Utilisé pour la navigation
    val label: String,         // Libellé affiché
    val icon: ImageVector,     // Icône associée
    val contentDescription: String? = null // Description pour l'accessibilité
) {
    Accueil("Accueil", "Accueil", Icons.Default.Home, "Page d'accueil"),
    Message("Rapport", "Message", Icons.Default.List, "Page de rapport"),
    Presence("Presence", "Présences", Icons.Default.Person, "Page des présences")
}
