package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.navigation.NavType
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import androidx.navigation.navArgument
import androidx.core.view.WindowCompat
import androidx.compose.material3.Surface
import androidx.activity.ComponentActivity
import androidx.navigation.compose.NavHost
import androidx.activity.compose.setContent
import androidx.navigation.compose.composable
import com.example.fooddeliveryapp.ui.screen.*
import com.example.fooddeliveryapp.ui.theme.AppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.BurgerScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.DrinksScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.FryScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.JuiceScreen
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.PastaScreen

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
                        composable("FryScreen") {
                            FryScreen(navController)
                        }

                        // Drinks Screen
                        composable("DrinksScreen") {
                            DrinksScreen(navController)
                        }

                        // Juices Screen
                        composable("JuiceScreen") {
                            JuiceScreen(navController)
                        }

                        // Pasta Screen
                        composable("PastaScreen") {
                            PastaScreen(navController)
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

                        composable("profileView") {
                            ProfileViewScreen(navController)
                        }
                        composable("profileEdit") {
                            ProfileEditScreen(navController)
                        }

                    }
                }
            }
        }
    }
}
