package com.example.fooddeliveryapp.ui.screen

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavController) {
    val cartViewModel: CartViewModel = viewModel() // Get CartViewModel instance
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ADMIN_EMAIL = "admin@culinario.com"
    val ADMIN_PASSWORD = "admin1234"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent) // Transparent background
                    .padding(paddingValues)
            ) {
                // Transparent Background Image
                Image(
                    painter = painterResource(id = R.drawable.bcground),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.food_logo1),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Name
                    Text(
                        text = "CULINARIO",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFA500),
                        fontSize = 30.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email TextBox
                    PlaceholderTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password TextBox
                    PlaceholderTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        keyboardType = KeyboardType.Password
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button
                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill all fields")
                                }
                                return@Button
                            }

                            // Handle admin login
                            if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Initialize admin's cart
                                            val user = auth.currentUser
                                            user?.uid?.let { uid ->
                                                cartViewModel.initialize(uid)
                                            }
                                            navController.navigate("adminHomeScreen") {
                                                popUpTo("signInScreen") { inclusive = true }
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Admin login failed")
                                            }
                                        }
                                    }
                                return@Button
                            }

                            // Regular user login
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        user?.let {
                                            cartViewModel.initialize(it.uid)
                                            navController.navigate("homeScreen") {
                                                popUpTo("signInScreen") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Login failed: ${task.exception?.message}")
                                        }
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFA500), Color(0xFFFF6347))
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent // Transparent to show gradient
                        )
                    ) {
                        Text(
                            text = "Login",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign-Up Redirect
                    TextButton(onClick = {
                        navController.navigate("signUpScreen") // Navigate to SignUpScreen
                    }) {
                        Text(
                            text = "Don't have an account? Click here",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun PlaceholderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color.LightGray.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray
                        )
                    )
                }
                innerTextField() // Render the input field
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
    AppTheme {
        SignInScreen(navController = NavController(LocalContext.current))
    }
}
