package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.JuiceScreen

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
                    val cartViewModel: CartViewModel = viewModel()

                    NavHost(navController = navController,
                        startDestination = "splashScreen") {
                        // Splash Screen
                        composable("splashScreen") {
                            SplashScreen {
                                navController.navigate("signInScreen") {
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

                        // Juices Screen
                        composable("JuiceScreen") {
                            JuiceScreen(navController)
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
                            "productDetailsScreen/{productType}/{productId}",
                            arguments = listOf(
                                navArgument("productType") { type = NavType.StringType },
                                navArgument("productId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val productType = backStackEntry.arguments?.getString("productType") ?: ""
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            ProductDetailsScreen(
                                productType = productType,
                                productId = productId,
                                navController = navController,
                                cardViewModel = cartViewModel
                            )
                        }

                        // Home Screen
                        composable("homeScreen") {
                            HomeScreen(navController)
                        }

                        composable("adminHomeScreen") {
                            AdminHomeScreen(navController)
                        }

                        // Search Bar Section
                        composable("SearchBarSection") {
                            SearchScreen()
                        }

                        composable("cart") {
                            AddToCartScreen(
                                navController = navController,
                                cartViewModel = cartViewModel
                            )
                        }

                        composable("AddToCartScreen") {
                            AddToCartScreen(
                                navController = navController,
                                cartViewModel = cartViewModel
                            )
                        }



                    }
                }
            }
        }
    }
}
