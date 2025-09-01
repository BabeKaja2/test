package com.babetech.ucb_admin_access.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babetech.ucb_admin_access.R
import com.babetech.ucb_admin_access.viewmodel.ScannerViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classes de données pour les notifications de badges détectés.
 */
data class DetectedBadge(
    val id: String,
    val timestamp: Long
)

/**
 * Fonction utilitaire pour déclencher une vibration.
 *
 * @param context Le contexte Android pour accéder au service de vibration.
 */
fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (vibrator.hasVibrator()) {
        // API 26 et plus (Android O)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            // Avant API 26
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }
}

/**
 * Écran principal du scanner qui simule la détection de badges BLE.
 *
 * @param viewModel Le ViewModel pour gérer la logique de l'écran et les données.
 */
@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    // Obtenir le contexte local pour utiliser le service de vibration
    val context = LocalContext.current

    // États observés depuis le ViewModel pour les informations de l'étudiant, la dernière mise à jour et les messages d'erreur.
    val student by viewModel.studentInfo.collectAsStateWithLifecycle()
    val lastFetched by viewModel.lastFetched.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // États locaux pour gérer l'interface utilisateur de l'écran.
    var isScanning by remember { mutableStateOf(false) } // Indique si le scan BLE est actif.
    // Ces listes seront désormais mises à jour par le ViewModel via de vraies détections,
    // ou resteront vides si le ViewModel ne fournit pas de données de détection.
    var detectedBadges by remember { mutableStateOf(listOf<DetectedBadge>()) } // Liste des badges BLE détectés.

    // Nouvel état pour la notification de détection d'étudiant
    var studentNotification by remember { mutableStateOf<DetectedBadge?>(null) }


    // Effet lancé pour le scan BLE.
    // Il ne contient plus de simulation de détection.
    LaunchedEffect(isScanning) {
        if (isScanning) {
            viewModel.startScanning() // Démarre le scan dans le ViewModel.
            // La logique de détection et d'ajout aux listes 'detectedBadges' devrait venir du ViewModel.
        } else {
            viewModel.stopScanning() // Arrête le scan dans le ViewModel.
        }
    }

    // Effet pour la suppression automatique des notifications de badges après 10 secondes.
    // Cette logique est conservée au cas où le ViewModel pousserait de vraies détections.
    LaunchedEffect(detectedBadges) {
        detectedBadges.forEach { badge ->
            delay(10000) // Attend 10 secondes.
            detectedBadges = detectedBadges.filter { it.timestamp != badge.timestamp } // Supprime le badge.
        }
    }

    // Effet pour afficher et faire disparaître la notification d'étudiant détecté
    LaunchedEffect(student) {
        // Ajout de la condition pour vérifier si le scan est actif
        if (isScanning) {
            student?.let { s ->
                // Déclenche la vibration lors de la détection d'un étudiant
                vibrate(context)
                // Crée une notification pour l'étudiant détecté, utilisant son matricule
                studentNotification = DetectedBadge(id = s.matricule, timestamp = System.currentTimeMillis())
                // Fait disparaître la notification après 3 secondes
                delay(3000)
                studentNotification = null
            }
        }
    }

    // Conteneur principal de l'écran avec un dégradé de fond.
    Column( // Changé de Box à Column pour empiler verticalement les éléments.
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F8FF), // Équivalent à Blue-50
                        Color(0xFFE0E7FF)  // Équivalent à Indigo-100
                    )
                )
            )
    ) {
        // Zone des notifications en haut de l'écran.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 200.dp), // Limite la hauteur des notifications pour éviter de cacher le contenu principal.
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espacement entre les notifications.
        ) {
            // Affiche la notification d'étudiant détecté
            studentNotification?.let { notification ->
                item(key = "student-${notification.timestamp}") {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        NotificationCard(
                            icon = rememberVectorPainter(image = Icons.Default.Person), // Icône de personne
                            title = "Étudiant détecté",
                            subtitle = "Matricule: ${notification.id}", // Affiche le matricule
                            backgroundColor = Color(0xFF06B6D4), // Couleur Cyan-500 pour cette notification
                            onDismiss = { studentNotification = null } // Permet de fermer manuellement
                        )
                    }
                }
            }

            // Affiche les notifications de badges BLE.
            items(detectedBadges, key = { "badge-${it.timestamp}" }) { badge ->
                AnimatedVisibility(
                    visible = true, // Toujours visible une fois ajoutée, l'animation gère l'entrée/sortie.
                    enter = slideInVertically() + fadeIn(), // Animation d'entrée.
                    exit = slideOutVertically() + fadeOut() // Animation de sortie.
                ) {
                    NotificationCard(
                        icon = painterResource(id = R.drawable.bluetooth), // Icône Bluetooth.
                        title = "Badge BLE détecté",
                        subtitle = badge.id,
                        backgroundColor = Color(0xFF10B981), // Vert-500.
                        onDismiss = {
                            // Supprime la notification lorsque l'utilisateur la ferme.
                            detectedBadges = detectedBadges.filter { it.timestamp != badge.timestamp }
                        }
                    )
                }
            }
        }

        // Interface principale centrée sur l'écran, prend l'espace restant.
        Column(
            modifier = Modifier
                .fillMaxWidth() // Remplit la largeur de la colonne parente.
                .weight(1f) // Prend tout l'espace vertical restant.
                .padding(horizontal = 24.dp), // Applique un padding horizontal.
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(), // Remplit la largeur de son parent Column.
                shape = RoundedCornerShape(24.dp), // Coins arrondis pour la carte principale.
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp) // Ombre de la carte.
            ) {
                Column(
                    modifier = Modifier.padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Titre de l'application.
                    Text(
                        text = "Scan",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Zone d'animation du radar/scanner.
                    RadarAnimation(
                        isScanning = isScanning
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Boutons de contrôle pour démarrer/arrêter les scans.
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bouton pour le scan BLE.
                        if (!isScanning) {
                            Button(
                                onClick = { isScanning = true }, // Démarre le scan BLE.
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB) // Bleu-600.
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bluetooth),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Scanner un badge BLE",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else {
                            Button(
                                onClick = { isScanning = false }, // Arrête le scan BLE.
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626) // Rouge-600.
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Arrêter le scan BLE",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Indicateur de statut du scan.
                    StatusIndicator(
                        isScanning = isScanning
                    )

                    // Compteurs de badges détectés.
                    if (detectedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (detectedBadges.isNotEmpty()) {
                                CounterCard(
                                    count = detectedBadges.size,
                                    label = "Badges BLE",
                                    color = Color(0xFF3B82F6) // Bleu-500.
                                )
                            }
                        }
                    }



                    }
                }
            }
        }
    }


/**
 * Carte de notification personnalisée.
 *
 * @param icon L'icône à afficher dans la notification.
 * @param title Le titre de la notification.
 * @param subtitle Le sous-titre de la notification.
 * @param backgroundColor La couleur de fond de la carte.
 * @param onDismiss L'action à effectuer lorsque la notification est fermée.
 */
@Composable
fun NotificationCard(
    icon: Painter,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Animation de radar/scanner.
 *
 * @param isScanning Indique si le scan BLE est actif pour l'animation.
 */
@Composable
fun RadarAnimation(
    isScanning: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    // Animations de mise à l'échelle pour les cercles du radar.
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )

    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale3"
    )

    Box(
        modifier = Modifier.size(128.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            val color = Color(0xFF3B82F6) // Couleur basée sur le type de scan (BLE uniquement).

            // Cercles animés du radar.
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .scale(scale1)
                    .border(4.dp, color.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .scale(scale2)
                    .border(4.dp, color.copy(alpha = 0.4f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale3)
                    .border(4.dp, color.copy(alpha = 0.5f), CircleShape)
            )

            // Cercles statiques du radar.
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .border(4.dp, Color(0xFFE5E7EB), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(4.dp, Color(0xFFD1D5DB), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(4.dp, Color(0xFFD1D5DB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter =  painterResource(id = R.drawable.bluetooth) , // Icône centrale.
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Affichage lorsque aucun scan n'est actif.
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .border(4.dp, Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bluetooth),
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Indicateur de statut de scan.
 *
 * @param isScanning Indique si le scan BLE est actif.
 */
@Composable
fun StatusIndicator(
    isScanning: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Détermine la couleur et le texte de l'indicateur.
        val (color, text) = when {
            isScanning -> Color(0xFF3B82F6) to "Scan BLE en cours..."
            else -> Color(0xFF6B7280) to "Aucun scan actif"
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
                .then(
                    // Animation de pulsation si un scan est actif.
                    if (isScanning) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        Modifier.background(color.copy(alpha = alpha), CircleShape)
                    } else Modifier
                )
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Carte de compteur pour afficher le nombre de badges ou de codes QR détectés.
 *
 * @param count Le nombre à afficher.
 * @param label Le libellé du compteur (ex: "Badges BLE").
 * @param color La couleur principale de la carte.
 */
@Composable
fun CounterCard(
    count: Int,
    label: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f) // Couleur de fond transparente.
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
