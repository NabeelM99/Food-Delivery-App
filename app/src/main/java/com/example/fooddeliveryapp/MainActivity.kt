package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fooddeliveryapp.ui.screen.* // Import all your screens here
import com.example.fooddeliveryapp.ui.theme.AppTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status bar transparent and ensure content doesn't go behind it
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splashScreen") {
                        // Splash Screen
                        composable("splashScreen") {
                            SplashScreen {
                                navController.navigate("homeScreen") {
                                    popUpTo("splashScreen") { inclusive = true }
                                }
                            }
                        }

                        // Location Selection Screen
                        composable("LocationScreen") {
                            LocationSelectionScreen(navController)
                        }

                        // Location Map Screen
                        composable("locationMapScreen") {
                            LocationMapScreen(navController)
                        }

                        // Sign In Screen
                        composable("signInScreen") {
                            SignInScreen(navController)
                        }

                        // Sign Up Screen
                        composable("signUpScreen") {
                            SignUpScreen(navController)
                        }

                        // Product Details Screen
                        composable("productDetailsScreen") {
                            ProductDetailsScreen() // Adjust if parameters are needed
                        }

                        // Home Screen
                        composable("homeScreen") {
                            HomeScreen(navController)
                        }

                        // Search Bar Section
                        composable("SearchBarSection") {
                            SearchScreen()
                        }
                    }
                }
            }
        }
    }
}
