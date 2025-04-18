package com.example.fooddeliveryapp.ui.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import com.example.fooddeliveryapp.ui.screen.home_components.BestOfferSection
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(navController: NavController) {
    var currentRoute by remember { mutableStateOf("home") }
    val scrollState = rememberScrollState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

    BackHandler {
        (context as Activity).finish()
    }

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
                            if (route == "SearchBarSection") {
                                navController.navigate("SearchBarSection") // Navigate to SearchScreen
                            } else {
                                navController.navigate(route)
                            }
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
                TopSection(navController = navController)
                Spacer(modifier = Modifier.height(2.dp))
                TodaysMenuSection()
                Spacer(modifier = Modifier.height(2.dp))
                MenuSection(navController)
                Spacer(modifier = Modifier.height(20.dp))
                BestOfferSection(navController)

                }
            }
        }
    }
