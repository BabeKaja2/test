package com.babetech.ucb_admin_access.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.babetech.ucb_admin_access.utils.AppDestinations
import com.babetech.ucb_admin_access.viewmodel.ScannerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.Accueil) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.contentDescription,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text(it.label, style = MaterialTheme.typography.labelSmall) },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("UCB Admin Access") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            ) {
                val viewModel: ScannerViewModel = koinViewModel()
                when (currentDestination) {
                    AppDestinations.Accueil -> ScannerScreen(viewModel)

                    AppDestinations.Message -> RapportScreen()
                }
            }
        }
    }
}
