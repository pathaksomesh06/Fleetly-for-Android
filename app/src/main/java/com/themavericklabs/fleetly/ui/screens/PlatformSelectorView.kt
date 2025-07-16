package com.themavericklabs.fleetly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.themavericklabs.fleetly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformSelectorView(
    navController: NavController,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = stringResource(R.string.logout_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.select_platform),
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PlatformCard(
                title = stringResource(R.string.all_platforms),
                icon = Icons.Default.Devices,
                onClick = { navController.navigate("deviceList") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlatformCard(
                title = stringResource(R.string.windows),
                icon = Icons.Default.Computer,
                onClick = { navController.navigate("deviceList?platform=windows") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlatformCard(
                title = stringResource(R.string.mac),
                icon = Icons.Default.LaptopMac,
                onClick = { navController.navigate("deviceList?platform=mac") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlatformCard(
                title = stringResource(R.string.ios),
                icon = Icons.Default.PhoneIphone,
                onClick = { navController.navigate("deviceList?platform=ios") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlatformCard(
                title = stringResource(R.string.android),
                icon = Icons.Default.PhoneAndroid,
                onClick = { navController.navigate("deviceList?platform=android") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 