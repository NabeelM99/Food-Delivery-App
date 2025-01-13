package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.example.fooddeliveryapp.ui.theme.extendedTypography

private val UnboundedFontFamily= FontFamily(
    Font(R.font.yummy_foodies_regular, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.yummy_foodies_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.yummy_foodies_regular, FontWeight.Light, FontStyle.Normal),
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create your Account",
                        color = Color(0xFFFFA500),
                        fontFamily = UnboundedFontFamily,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        content = { paddingValues ->
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

                )

                Spacer(modifier = Modifier.height(16.dp))

                // App Name
                Text(
                    text = "CULINARIO",
                    style = extendedTypography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp
                )

                // Name TextBox
                PlaceholderTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Name",
                    keyboardType = KeyboardType.Text,
                    //fontFamily = YummyFoodiesFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email TextBox
                PlaceholderTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email,
                    //fontFamily = YummyFoodiesFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password TextBox
                PlaceholderTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    keyboardType = KeyboardType.Password,
                    //fontFamily = YummyFoodiesFontFamily
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Create Account Button
                Button(
                    onClick = {
                        // Handle account creation logic here (e.g., save user to database)
                        navController.navigate("loginScreen") // After sign-up, navigate to login screen
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
                        fontFamily = UnboundedFontFamily,
                        style = MaterialTheme.typography.bodyLarge
                    )
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
