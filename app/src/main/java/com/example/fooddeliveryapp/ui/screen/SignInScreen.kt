package com.example.fooddeliveryapp.ui.screen

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val validEmail = "test@example.com"
    val validPassword = "password123"

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Name
                Text(
                    text = "CULINARIO",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Text Field with Placeholder
                PlaceholderTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Text Field with Placeholder
                PlaceholderTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    keyboardType = KeyboardType.Password
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Button(
                    onClick = {
                        if (email == validEmail && password == validPassword) {
                            navController.navigate("productDetailsScreen") // Navigate to ProductDetailsScreen
                        } else {
                            // Handle invalid login (e.g., Toast/Snackbar)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign-Up Redirect
                TextButton(onClick = {
                    navController.navigate("signUpScreen") // Navigate to SignUpScreen
                }) {
                    Text(text = "Don't have an account? Sign up here", color = MaterialTheme.colorScheme.primary)
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
                color = Color.LightGray.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp), // Align cursor to center
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray,

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
