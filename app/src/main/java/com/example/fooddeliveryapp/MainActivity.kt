package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fooddeliveryapp.ui.screen.* // Import all your screens here
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.BurgerScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.DrinksScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.FriesScreen
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

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

                        // Burger Screen
                        composable("BurgerScreen") {
                            BurgerScreen(navController)
                        }

                        // Fries Screen
                        composable("FriesScreen") {
                            FriesScreen(navController)
                        }

                        // Drinks Screen
                        composable("DrinksScreen") {
                            DrinksScreen(navController)
                        }

                        // Location Selection Screen
                        composable("location") {
                            LocationMapScreen(navController)
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

                        // Product Details Screen with burgerId argument
                        composable(
                            "productDetailsScreen/{burgerId}",
                            arguments = listOf(navArgument("burgerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val burgerId = backStackEntry.arguments?.getString("burgerId") ?: ""
                            ProductDetailsScreen(burgerId = burgerId, navController = navController)
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
