package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import com.example.fooddeliveryapp.viewmodel.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private val YummyFoodiesFontFamily = FontFamily(
    Font(R.font.yummy_foodies_regular, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.bcground),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Semi-Transparent Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f)) // Adjust alpha for opacity
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.food_logo1),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(250.dp)
                            .padding(bottom = 24.dp)

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Name
                    Text(
                        text = "Create your Account",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        //style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFA500),
                        fontSize = 25.sp,
                        modifier = Modifier.padding(vertical = 16.dp)

                    )

                    Spacer(modifier = Modifier.height(24.dp)) // Add this between components

                    // Name TextBox
                    PlaceholderTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Name",
                        keyboardType = KeyboardType.Text,
                        //fontFamily = YummyFoodiesFontFamily
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Email TextBox
                    PlaceholderTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email,
                        //fontFamily = YummyFoodiesFontFamily
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Password TextBox
                    PlaceholderTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        keyboardType = KeyboardType.Password,
                        //fontFamily = YummyFoodiesFontFamily
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Create Account Button
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        /*if (task.isSuccessful) {
                                            // Navigate to login screen or home
                                            navController.navigate("signInScreen")
                                        }*/
                                        if (task.isSuccessful) {
                                            // Get the newly created user
                                            val user = auth.currentUser
                                            user?.let {
                                                // Create a default profile document in Firestore
                                                FirebaseFirestore.getInstance().collection("users")
                                                    .document(user.uid)
                                                    .set(
                                                        UserProfile(
                                                            name = name,
                                                            email = email,
                                                            mobile = "",
                                                            address = "",
                                                            dob = ""
                                                        )
                                                    )
                                                    .addOnSuccessListener {
                                                        // Navigate after Firestore write succeeds
                                                        navController.navigate("signInScreen")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Handle Firestore error
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Failed to create profile: ${e.message}")
                                                        }
                                                    }
                                            }
                                        } else {
                                            val message = task.exception?.message ?: "Sign-up failed"

                                            scope.launch {
                                                snackbarHostState.showSnackbar(message) // Show error message in Snackbar
                                            }
                                        }
                                    }
                            }
                        },
                        colors = ButtonDefaults
                            .buttonColors(containerColor = Color(0xFFFFA500)), // Orange color
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Create Account",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

        }
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    AppTheme {
        SignUpScreen(navController = NavController(LocalContext.current))
    }
}
