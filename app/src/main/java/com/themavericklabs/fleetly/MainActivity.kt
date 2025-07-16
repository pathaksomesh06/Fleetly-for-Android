package com.themavericklabs.fleetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.themavericklabs.fleetly.ui.navigation.FleetlyNavGraph
import com.themavericklabs.fleetly.ui.theme.FleetlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FleetlyTheme {
                val navController = rememberNavController()
                FleetlyNavGraph(navController = navController)
            }
        }
    }
} 