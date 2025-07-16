package com.themavericklabs.fleetly.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.themavericklabs.fleetly.R
import com.themavericklabs.fleetly.ui.viewmodels.AuthViewModel

@Composable
fun WelcomeView(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val isConfigured by authViewModel.isConfigured.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val error by authViewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.checkInitialAuth()
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onLoginSuccess()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isConfigured) {
                LoginContent(
                    isLoading = isLoading,
                    onLoginClick = {
                        (context as? Activity)?.let {
                            authViewModel.login(it)
                        }
                    }
                )
            } else {
                EnterpriseOnlyContent()
            }
        }
    }


    error?.let {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            title = { Text("Authentication Failed") },
            text = { Text(it.message) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun LoginContent(isLoading: Boolean, onLoginClick: () -> Unit) {
    Text(
        text = "Welcome to Fleetly",
        style = MaterialTheme.typography.headlineLarge
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Sign in to continue",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Login")
        }
    }
}

@Composable
fun EnterpriseOnlyContent() {
    Icon(
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = "Fleetly Logo",
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Welcome to Fleetly",
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(32.dp))
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Warning",
        modifier = Modifier.size(48.dp),
        tint = Color(0xFFFFA900)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Enterprise Distribution Only",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "This app needs to be deployed through your organization's device management system to receive necessary configuration",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = Color.Gray
    )
    Spacer(modifier = Modifier.height(24.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Steps to Configure:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Contact your IT administrator")
            Spacer(modifier = Modifier.height(4.dp))
            Text("• App requires deployment via enterprise mobility management (MDM)")
            Spacer(modifier = Modifier.height(4.dp))
            Text("• Obtain required configuration settings")
        }
    }
} 