package com.example.fooddeliveryapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.components.BottomNavBar
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Red
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val userProfile by profileViewModel.userProfile.collectAsState()
    var currentRoute by remember { mutableStateOf("") }
    val cartViewModel: CartViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()

    // Animation states
    val coroutineScope = rememberCoroutineScope()
    var isLoaded by remember { mutableStateOf(false) }
    val profileImageScale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutElastic),
        label = "profileImageScale"
    )

    val headerTextAlpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "headerTextAlpha"
    )

    val infoItemsVisibility = remember { mutableStateListOf<Boolean>().apply {
        repeat(5) { add(false) }
    }}

    // Floating button animation
    val fabExpanded = remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(
        targetValue = if (fabExpanded.value) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fabRotation"
    )

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        // Staggered animations
        delay(300)
        isLoaded = true
        delay(500)
        infoItemsVisibility.forEachIndexed { index, _ ->
            delay(150L * index)
            infoItemsVisibility[index] = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Orange, Red)
                    )
                )
        )

        // Profile content
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            currentRoute = route
                            navController.navigate(route)
                        }
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        fabExpanded.value = !fabExpanded.value
                        if (!fabExpanded.value) {
                            navController.navigate("profileEdit")
                        }
                    },
                    containerColor = Orange,
                    contentColor = Color.White,
                    //expanded = true,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .graphicsLayer {
                            rotationZ = fabRotation
                        }
                ) {
                    Icon(
                        imageVector = if (fabExpanded.value) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                    AnimatedVisibility(
                        visible = !fabExpanded.value,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Text(
                            "Edit Profile",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    //.verticalScroll(rememberScrollState()) // Make the whole screen scrollable
            ) {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(70.dp))

                        // Profile Picture with animation
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(profileImageScale)
                                .shadow(16.dp, CircleShape)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = userProfile?.profilePicture?.takeIf { it.isNotEmpty() }
                                    ?: R.drawable.food_logo1,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Profile text with fade-in animation
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Profile",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 30.sp,
                            modifier = Modifier.alpha(headerTextAlpha)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userProfile?.email ?: "user@example.com",
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .alpha(headerTextAlpha)
                        )
                    }
                }

                // Profile Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-30).dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            //.fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 24.dp, horizontal = 20.dp)
                    ) {
                        // Account Information Section with staggered animations
                        Text(
                            text = "Account Info",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        AccountInfoItem(
                            icon = Icons.Outlined.Person,
                            label = "Name",
                            value = userProfile?.name ?: "Not set",
                            isVisible = infoItemsVisibility.getOrElse(0) { false }
                        )
                        AccountInfoItem(
                            icon = Icons.Outlined.Phone,
                            label = "Mobile",
                            value = userProfile?.mobile ?: "Not set",
                            isVisible = infoItemsVisibility.getOrElse(1) { false }
                        )
                        AccountInfoItem(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = FirebaseAuth.getInstance().currentUser?.email ?: "Not set",
                            isVisible = infoItemsVisibility.getOrElse(2) { false }
                        )
                        AccountInfoItem(
                            icon = Icons.Outlined.Home,
                            label = "Address",
                            value = userProfile?.address ?: "Not set",
                            isVisible = infoItemsVisibility.getOrElse(3) { false }
                        )
                        AccountInfoItem(
                            icon = Icons.Outlined.DateRange,
                            label = "D.O.B",
                            value = userProfile?.dob ?: "Not set",
                            isVisible = infoItemsVisibility.getOrElse(4) { false }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Logout button with animation - more prominently positioned
                        var isLogoutLoading by remember { mutableStateOf(false) }

                        AnimatedVisibility(
                            visible = isLoaded,
                            enter = fadeIn(animationSpec = tween(1000)) + expandVertically()
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLogoutLoading = true
                                        delay(1000) // Simulate loading

                                        // Sign out from Firebase
                                        auth.signOut()

                                        // Clear cart data
                                        cartViewModel.clearCart()

                                        // Navigate to SignInScreen and reset back stack
                                        navController.navigate("signInScreen") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true // Closes all previous screens
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .animateContentSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Red,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLogoutLoading
                            ) {
                                if (isLogoutLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_logout),
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Logout",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Add extra space at bottom to ensure visibility with bottom nav
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Orange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = label,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}