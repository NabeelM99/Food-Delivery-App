package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ui.screen.home_components.MenuSection
import com.example.fooddeliveryapp.ui.screen.home_components.TodaysMenuSection
import com.example.fooddeliveryapp.ui.screen.home_components.TopSection
import com.example.fooddeliveryapp.components.BottomNavBar

@Composable
fun HomeScreen(navController: NavController) {
    var currentRoute by remember { mutableStateOf("home") }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            currentRoute = route
                            navController.navigate(route)
                        }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopSection()
                TodaysMenuSection()
                MenuSection()

                Spacer(modifier = Modifier.height(20.dp))
                Text("Welcome to the Home Screen!")
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { /* Handle navigation or actions here */ }) {
                    Text("Explore More")
                }
            }
        }
    }
}