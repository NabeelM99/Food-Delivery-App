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
import androidx.compose.ui.unit.TextUnit
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

    // Animation states
    val logoAnimatable = remember { Animatable(0.8f) }
    val formVisibility = remember { MutableTransitionState(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Initialize animation
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

                    // Animated app name
                    PulsatingText(
                        text = "CULINARIO",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFA500),
                        fontSize = 50.sp,
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
                                        modifier = Modifier.size(25.dp)
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

                            // Login Button with animation
                            LoginButton(
                                onClick = {
                                    if (email.isEmpty() || password.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Please fill all fields")
                                        }
                                        return@LoginButton
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
                                        return@LoginButton
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

                            // Sign-Up Redirect with animation
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
                                    color = Color.Black,
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

// Animated shimmer effect for text fields
@Composable
fun AnimatedShimmerTextField(
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

    // Shimmer animation
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Border animation
    val borderColor = animateColorAsState(
        targetValue = if (isFocused) Color(0xFFFFA500) else Color.Transparent,
        animationSpec = tween(300)
    )

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
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (isFocused) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = shimmerColors,
                            startX = translateAnim.value - 1000f,
                            endX = translateAnim.value
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.background(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            )
            .border(
                width = 2.dp,
                color = borderColor.value,
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

// Animated login button
@Composable
fun LoginButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

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
            .graphicsLayer {
                this.shadowElevation = shadowElevation.value
                shape = RoundedCornerShape(50.dp)
            }
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
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
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

// Pulsating text animation
@Composable
fun PulsatingText(
    text: String,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = text,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        textAlign = textAlign,
        color = color,
        //fontSize = fontSize.sp,
        modifier = modifier
            .scale(scale.value)
            .graphicsLayer {
                this.shadowElevation = 5f
            }
    )
}

// Gradient text
@Composable
fun GradientText(
    text: String,
    gradient: Brush,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        //fontSize = fontSize.sp,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.Transparent
        ),
        modifier = modifier
            .background(gradient)
            .alpha(0.99f)
    )
}

// Food particles for background animation
data class ParticleState(
    val x: Float = (0..1000).random().toFloat(),
    val y: Float = (0..2000).random().toFloat(),
    val scale: Float = (5..15).random() / 10f,
    val speed: Float = (1..5).random() / 10f,
    val rotationSpeed: Float = (-5..5).random().toFloat(),
    val iconRes: Int = listOf(
        R.drawable.ic_pizza,
        R.drawable.ic_burger,
        R.drawable.ic_sushi,
        R.drawable.ic_salad,
        R.drawable.ic_coffee
    ).random() // You'll need to add these food icon resources
)

@Composable
fun FoodParticle(particle: ParticleState) {
    val infiniteTransition = rememberInfiniteTransition()

    val yPosition = infiniteTransition.animateFloat(
        initialValue = particle.y,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (15000 / particle.speed).toInt(),
                easing = LinearEasing
            )
        )
    )

    val xPosition = infiniteTransition.animateFloat(
        initialValue = particle.x,
        targetValue = particle.x + 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (20000 / particle.speed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (5000 / particle.rotationSpeed.coerceAtLeast(0.1f)).toInt(),
                easing = LinearEasing
            )
        )
    )

    Icon(
        painter = painterResource(id = particle.iconRes),
        contentDescription = null,
        tint = Color(0xFFFFA500).copy(alpha = 0.2f),
        modifier = Modifier
            .offset(x = xPosition.value.dp, y = yPosition.value.dp)
            .size((24 * particle.scale).dp)
            .graphicsLayer {
                rotationZ = if (particle.rotationSpeed != 0f) rotation.value else 0f
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