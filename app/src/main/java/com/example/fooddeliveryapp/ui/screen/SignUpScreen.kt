package com.example.fooddeliveryapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.UserProfile
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Animation states
    val logoAnimatable = remember { Animatable(0.8f) }
    val formVisibility = remember { MutableTransitionState(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Initialize animation
    LaunchedEffect(Unit) {
        delay(300)
        logoAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        formVisibility.targetState = true
    }

    // Particle animation for background
    val particles = remember { List(20) { ParticleState() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                // Animated background
                Image(
                    painter = painterResource(id = R.drawable.bcground),
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(3.dp),
                    contentScale = ContentScale.Crop
                )

                // Semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.7f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Floating food particles
                particles.forEach { particle ->
                    FoodParticle(particle)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated logo
                    Image(
                        painter = painterResource(id = R.drawable.food_logo1),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(bottom = 16.dp)
                            .scale(logoAnimatable.value)
                            .graphicsLayer {
                                rotationZ = logoAnimatable.value * 10 * (if (logoAnimatable.value >= 0.95f) 0f else 1f)
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Animated header text
                    PulsatingText(
                        text = "CREATE ACCOUNT",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFA500),
                        fontSize = 34.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated form
                    AnimatedVisibility(
                        visibleState = formVisibility,
                        enter = fadeIn(animationSpec = tween(1000)) +
                                expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    expandFrom = Alignment.Top
                                ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFA500).copy(alpha = 0.5f),
                                            Color(0xFFFF6347).copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            // Name field
                            AnimatedShimmerTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Name",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_person),
                                        contentDescription = "Name Icon",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            // Email field
                            AnimatedShimmerTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_email),
                                        contentDescription = "Email Icon",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            // Password field
                            AnimatedShimmerTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lock),
                                        contentDescription = "Password Icon",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible }
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (passwordVisible) R.drawable.ic_visibility_on
                                                else R.drawable.ic_visibility_off
                                            ),
                                            contentDescription = "Toggle password visibility",
                                            tint = Color(0xFFFFA500),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )

                            // Create Account Button
                            LoginButton(
                                onClick = {
                                    if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please fill all fields")
                                        }
                                        return@LoginButton
                                    }

                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                user?.let {
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
                                                            navController.navigate("signInScreen") {
                                                                popUpTo("signUpScreen") { inclusive = true }
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Failed to create profile: ${e.message}")
                                                            }
                                                        }
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Signup failed: ${task.exception?.message}")
                                                }
                                            }
                                        }
                                },
                                text = "Create Account",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Login Redirect with animation
                            val loginScale = remember { Animatable(1f) }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        loginScale.animateTo(0.9f, tween(100))
                                        loginScale.animateTo(1f, tween(100))
                                        navController.navigate("signInScreen")
                                    }
                                },
                                modifier = Modifier.scale(loginScale.value)
                            ) {
                                GradientText(
                                    text = "Already have an account? Login Here",
                                    gradient = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFA500),
                                            Color(0xFFFF6347)
                                        )
                                    ),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}