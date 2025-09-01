package com.babetech.ucb_admin_access.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn // This is what makes it scrollable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babetech.ucb_admin_access.viewmodel.RapportViewModel
import com.babetech.ucb_admin_access.viewmodel.StudentCached
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RapportScreen(
    viewModel: RapportViewModel = koinViewModel()
) {
    val students by viewModel.students.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Rapport des étudiants",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                exportStudentsToPdf(context =context,
                    students = students) }) {
                Text("Exporter en PDF")
            }
        }
        Spacer(Modifier.height(16.dp))

        // This LazyColumn handles the scrolling for the list of students
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Matricule: ${student.matricule}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Nom: ${student.fullname}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Filière: ${student.schoolFilieres ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Orientation: ${student.schoolOrientations ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        student.lastFetched.let { ts ->
                            ts?.let {
                                val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(Date(it))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Dernière mise à jour: $formatted",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}