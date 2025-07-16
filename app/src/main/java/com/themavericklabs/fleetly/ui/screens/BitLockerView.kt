package com.themavericklabs.fleetly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.themavericklabs.fleetly.R
import com.themavericklabs.fleetly.data.models.BitLockerRecoveryKey
import com.themavericklabs.fleetly.ui.viewmodels.BitLockerViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.ContentCopy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitLockerView(
    onBackPress: () -> Unit,
    viewModel: BitLockerViewModel = hiltViewModel()
) {
    val keys by viewModel.keys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.bitlocker_recovery_keys)) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(
                    text = error?.message ?: "An unknown error occurred.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(keys) { key ->
                        BitLockerKeyCard(key = key, error = if (key.key == null) error else null)
                    }
                }
            }
        }
    }
}

@Composable
fun BitLockerKeyCard(key: BitLockerRecoveryKey, error: com.themavericklabs.fleetly.ui.viewmodels.AlertError?) {
    val formattedDate = remember(key.createdDateTime) {
        key.createdDateTime?.let {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
            sdf.format(it)
        } ?: "N/A"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Recovery Key", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (key.key != null) {
                Text(text = key.key, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = error?.message ?: "Key not retrieved. You may not have the required permissions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Volume Type", style = MaterialTheme.typography.bodySmall)
                Text(text = key.volumeType ?: "N/A", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Created Date", style = MaterialTheme.typography.bodySmall)
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
} 