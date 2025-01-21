package com.example.fooddeliveryapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.*
import com.example.fooddeliveryapp.ui.screen.* // Import all your screens here
import com.example.fooddeliveryapp.ui.screen.HomeScreen
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

                        // Sign In screen
                        composable("signInScreen") {
                            SignInScreen(navController)
                        }

                        // Sign Up screen
                        composable("signUpScreen") {
                            SignUpScreen(navController)
                        }

                        // Product Details screen
                        composable("productDetailsScreen") {
                            ProductDetailsScreen() // Pass no extra parameters here
                        }

                        // Home screen after login/signup
                        composable("homeScreen") {
                            HomeScreen(navController) // Change this to your actual Home screen
                        }
                        composable("SearchBarSection") {
                            SearchScreen() // Change this to your actual Home screen
                        }

                    }
                }
            }
        }
    }
}