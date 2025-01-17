package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ui.screen.home_components.TodaysMenuSection
import com.example.fooddeliveryapp.ui.screen.home_components.TopSection


@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TopSection()
                //Spacer(modifier = Modifier.height(5.dp))
                TodaysMenuSection()
                Spacer(modifier = Modifier.height(20.dp))
                Text("Welcome to the Home Screen!")
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { /* Handle navigation or actions here */ }) {
                    Text("Explore More")
                }
            }
        }
    )
}
