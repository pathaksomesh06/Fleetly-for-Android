package com.themavericklabs.fleetly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.themavericklabs.fleetly.R
import com.themavericklabs.fleetly.data.models.DeviceAction
import com.themavericklabs.fleetly.data.models.IntuneDevice
import com.themavericklabs.fleetly.ui.viewmodels.DeviceDetailViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailView(
    deviceId: String,
    navController: NavController,
    onBackPress: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel()
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.actionStatus.collect { status ->
            snackbarHostState.showSnackbar(status)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_details)) },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            device?.let {
                DeviceDetailContent(
                    device = it,
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun DeviceDetailContent(device: IntuneDevice, modifier: Modifier = Modifier, viewModel: DeviceDetailViewModel, navController: NavController) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Device Information
        SectionTitle(title = stringResource(R.string.device_information))
        InfoRow(label = stringResource(R.string.device_name), value = device.name)
        InfoRow(label = stringResource(R.string.model), value = device.model)
        InfoRow(label = stringResource(R.string.manufacturer), value = device.manufacturer)
        device.osVersion?.let { InfoRow(label = stringResource(R.string.os_version), value = it) }

        Spacer(modifier = Modifier.height(16.dp))

        // Management Status
        SectionTitle(title = stringResource(R.string.management_status))
        InfoRow(label = stringResource(R.string.compliance_status), value = device.complianceStatus)
        InfoRow(label = stringResource(R.string.jailbroken), value = if (device.isJailbroken) "Yes" else "No")
        InfoRow(label = stringResource(R.string.enrollment_date), value = device.enrollmentDate.toString())
        InfoRow(label = stringResource(R.string.management_state), value = device.managementState)

        Spacer(modifier = Modifier.height(16.dp))

        // Owner Information
        SectionTitle(title = stringResource(R.string.owner_information))
        device.userPrincipalName?.let { InfoRow(label = stringResource(R.string.user_principal_name), value = it) }
        InfoRow(label = stringResource(R.string.owner_type), value = device.ownerType)

        Spacer(modifier = Modifier.height(16.dp))

        // Device Actions
        SectionTitle(title = stringResource(R.string.device_actions))
        DeviceActions(device = device, viewModel = viewModel, navController = navController)
    }
}

@Composable
fun DeviceActions(device: IntuneDevice, viewModel: DeviceDetailViewModel, navController: NavController) {
    Column {
        ActionRow(icon = Icons.Default.Sync, text = stringResource(R.string.sync_device), onClick = { viewModel.performAction(DeviceAction.SYNC) })
        if (device.model.equals("Windows", ignoreCase = true)) {
            ActionRow(icon = Icons.Default.VpnKey, text = stringResource(R.string.laps_credentials), onClick = { navController.navigate("laps/${device.id}") })
            ActionRow(icon = Icons.Default.Security, text = stringResource(R.string.bitlocker_recovery_keys), onClick = { navController.navigate("bitlocker/${device.id}") })
            ActionRow(icon = Icons.Default.RotateRight, text = stringResource(R.string.rotate_admin_password), onClick = { viewModel.performAction(DeviceAction.ROTATE_ADMIN_PASSWORD) })
            ActionRow(icon = Icons.Default.Cached, text = stringResource(R.string.fresh_start), onClick = { viewModel.performAction(DeviceAction.FRESH_START) })
            ActionRow(icon = Icons.Default.Autorenew, text = stringResource(R.string.autopilot_reset), onClick = { viewModel.performAction(DeviceAction.AUTOPILOT_RESET) })
            ActionRow(icon = Icons.Default.Assessment, text = stringResource(R.string.collect_diagnostic_data), onClick = { viewModel.performAction(DeviceAction.COLLECT_DIAGNOSTIC_DATA) })
        }
        if (device.model.equals("Mac", ignoreCase = true)) {
            ActionRow(icon = Icons.Default.VpnKey, text = "FileVault Key", onClick = { navController.navigate("fileVault/${device.id}") })
        }
        ActionRow(icon = Icons.Default.RestartAlt, text = stringResource(R.string.restart_device), onClick = { viewModel.performAction(DeviceAction.RESTART) })
        ActionRow(icon = Icons.Default.PowerSettingsNew, text = stringResource(R.string.shut_down_device), onClick = { viewModel.performAction(DeviceAction.SHUTDOWN) })
        ActionRow(icon = Icons.Default.Lock, text = stringResource(R.string.remote_lock), onClick = { viewModel.performAction(DeviceAction.REMOTE_LOCK) })
        ActionRow(icon = Icons.Default.PersonRemove, text = stringResource(R.string.retire_device), onClick = { viewModel.performAction(DeviceAction.RETIRE) })
        ActionRow(icon = Icons.Default.Delete, text = stringResource(R.string.wipe_device), onClick = { viewModel.performAction(DeviceAction.WIPE) })
        ActionRow(icon = Icons.Default.DeleteForever, text = stringResource(R.string.delete_device), onClick = { viewModel.performAction(DeviceAction.DELETE) })
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ActionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onSurface)
        }
    }
} 