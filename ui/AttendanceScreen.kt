package com.babetech.ucb_admin_access.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babetech.ucb_admin_access.domain.usecase.AttendanceRecord
import com.babetech.ucb_admin_access.viewmodel.AttendanceViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // En-tête avec titre et boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Présences",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtres"
                    )
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section des filtres (repliable)
        AnimatedVisibility(visible = showFilters) {
            FilterSection(
                selectedDate = uiState.selectedDate,
                selectedPromotion = uiState.selectedPromotion,
                selectedFaculte = uiState.selectedFaculte,
                availablePromotions = uiState.availablePromotions,
                availableFacultes = uiState.availableFacultes,
                onDateChange = viewModel::updateDateFilter,
                onPromotionChange = viewModel::updatePromotionFilter,
                onFaculteChange = viewModel::updateFaculteFilter
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistiques rapides
        StatsSection(attendanceRecords = uiState.attendanceRecords)

        Spacer(modifier = Modifier.height(16.dp))

        // Gestion des états d'erreur
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Liste des présences
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.attendanceRecords) { record ->
                    AttendanceCard(record = record)
                }
                
                if (uiState.attendanceRecords.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedDate: String,
    selectedPromotion: String?,
    selectedFaculte: String?,
    availablePromotions: List<String>,
    availableFacultes: List<String>,
    onDateChange: (String) -> Unit,
    onPromotionChange: (String?) -> Unit,
    onFaculteChange: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filtres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Filtre par date
            OutlinedTextField(
                value = selectedDate,
                onValueChange = onDateChange,
                label = { Text("Date (yyyy-MM-dd)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Filtre par promotion
            DropdownMenuField(
                label = "Promotion",
                selectedValue = selectedPromotion,
                options = availablePromotions,
                onValueChange = onPromotionChange,
                placeholder = "Toutes les promotions"
            )

            // Filtre par faculté
            DropdownMenuField(
                label = "Faculté",
                selectedValue = selectedFaculte,
                options = availableFacultes,
                onValueChange = onFaculteChange,
                placeholder = "Toutes les facultés"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuField(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onValueChange: (String?) -> Unit,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(placeholder) },
                onClick = {
                    onValueChange(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatsSection(attendanceRecords: List<AttendanceRecord>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Total",
            value = attendanceRecords.size.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Présents",
            value = attendanceRecords.count { it.isPresent }.toString(),
            color = Color(0xFF10B981), // Vert
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Promotions",
            value = attendanceRecords.mapNotNull { it.promotion }.distinct().size.toString(),
            color = Color(0xFF3B82F6), // Bleu
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun AttendanceCard(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (record.isPresent) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.studentFullname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Matricule: ${record.studentMatricule}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    record.promotion?.let { promotion ->
                        Chip(
                            text = promotion,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    record.studentFiliere?.let { filiere ->
                        Chip(
                            text = filiere,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = record.attendanceTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (record.isPresent) "Présent" else "Absent",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (record.isPresent) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucune présence enregistrée",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Les présences apparaîtront ici une fois détectées",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}