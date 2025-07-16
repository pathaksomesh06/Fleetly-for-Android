package com.themavericklabs.fleetly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.themavericklabs.fleetly.ui.viewmodels.LAPSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LAPSView(
    onBackPress: () -> Unit,
    viewModel: LAPSViewModel = hiltViewModel()
) {
    val lapsResponse by viewModel.lapsResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LAPS Credentials") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = error?.message ?: "An unknown error occurred")
            } else if (lapsResponse != null && lapsResponse?.credentials?.isNotEmpty() == true) {
                val credential = lapsResponse!!.credentials.first()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Local Admin Password",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = credential.passwordBase64?.let { viewModel.decodePassword(it) } ?: "N/A",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Password set on: ${credential.backupDateTime?.toString() ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(text = "No LAPS credentials found for this device.")
            }
        }
    }
} 