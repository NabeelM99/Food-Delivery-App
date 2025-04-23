package com.example.fooddeliveryapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(navController: NavController) {
    val cartViewModel: CartViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ADMIN_EMAIL = "admin@culinario.com"
    val ADMIN_PASSWORD = "admin1234"

    // Animation states - simplified
    val logoAnimatable = remember { Animatable(0.8f) }
    val formVisibility = remember { MutableTransitionState(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Initialize animation - but only once
    LaunchedEffect(Unit) {
        // Logo scaling animation
        delay(300)
        logoAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

        // Form appearance
        formVisibility.targetState = true
    }

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
                // Simplified background - static image with overlay
                Image(
                    painter = painterResource(id = R.drawable.bcground),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize().blur(3.dp),
                    contentScale = ContentScale.Crop
                )

                // Semi-transparent overlay - simplified
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

                // Removed floating food particles for performance

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo with simple animation
                    Image(
                        painter = painterResource(id = R.drawable.food_logo1),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(bottom = 16.dp)
                            .scale(logoAnimatable.value)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // App name - static instead of pulsating
                    Text(
                        text = "CULINARIO",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFA500),
                        fontSize = 30.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated form entry - kept for visual appeal
                    AnimatedVisibility(
                        visibleState = formVisibility,
                        enter = fadeIn(animationSpec = tween(800)) +
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
                                    color = Color(0xFFFFA500).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            // Optimized text fields
                            OptimizedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_email),
                                        contentDescription = "Email Icon",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(25.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )

                            // Password field - optimized
                            OptimizedTextField(
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
                                        modifier = Modifier.size(25.dp)
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
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )

                            // Optimized login button
                            OptimizedLoginButton(
                                onClick = {
                                    if (email.isEmpty() || password.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please fill all fields")
                                        }
                                        return@OptimizedLoginButton
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
                                        return@OptimizedLoginButton
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
                                text = "Login",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sign-Up Redirect - simplified animation
                            val signUpScale = remember { Animatable(1f) }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        signUpScale.animateTo(
                                            targetValue = 0.9f,
                                            animationSpec = tween(100)
                                        )
                                        signUpScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(100)
                                        )
                                        navController.navigate("signUpScreen")
                                    }
                                },
                                modifier = Modifier.scale(signUpScale.value)
                            ) {
                                Text(
                                    text = "Don't have an account? Click here",
                                    color = Color(0xFFFFA500),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.alpha(0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// Performance-optimized text field without shimmer animation
@Composable
fun OptimizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Simplified border animation
    val borderColor = if (isFocused) Color(0xFFFFA500) else Color.Transparent

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier = modifier
            .height(60.dp)
            .background(
                color = Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            color = Color.Gray.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerTextField()
                }

                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    )
}

// Simplified login button with minimal animation
@Composable
fun OptimizedLoginButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    // Simplified animation - just scale
    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val shadowElevation = animateFloatAsState(
        targetValue = if (isPressed.value) 2f else 8f,
        animationSpec = tween(100)
    )

    val gradientColors = listOf(Color(0xFFFFA500), Color(0xFFFF6347))

    val transition = rememberInfiniteTransition()
    val gradientAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        )
    )


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(50.dp)
            .scale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = gradientAnim.value - 1000f,
                    endX = gradientAnim.value
                ),
                shape = RoundedCornerShape(50.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50.dp)
            )
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
    AppTheme {
        SignInScreen(navController = NavController(LocalContext.current))
    }
}