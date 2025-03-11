package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.fooddeliveryapp.components.BottomNavBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Red
import com.google.firebase.auth.FirebaseAuth



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val userProfile by profileViewModel.userProfile.collectAsState()
    var currentRoute by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }


    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
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
                onClick = {navController.navigate("profileEdit")},
                containerColor = Orange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit,"Edit Profile")
            }
        },
        topBar = {
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
            {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))

                    // Profile Picture
                    AsyncImage(
                        model = userProfile?.profilePicture?.takeIf { it.isNotEmpty() }
                            ?: R.drawable.food_logo1,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Profile",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFFFFF),
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile?.email ?: "user@example.com",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                // Account Information Section
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Account Info",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AccountInfoItem(Icons.Default.Person, "Name", userProfile?.name ?: "Not set")
                    AccountInfoItem(Icons.Default.Phone, "Mobile", userProfile?.mobile ?: "Not set")
                    AccountInfoItem(Icons.Default.Email, "Email", FirebaseAuth.getInstance().currentUser?.email ?: "Not set")
                    AccountInfoItem(Icons.Default.Home, "Address", userProfile?.address ?: "Not set")
                    AccountInfoItem(Icons.Default.DateRange, "D.O.B", userProfile?.dob ?: "Not set")
                }
            }
        }
    }
}

@Composable
private fun AccountInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Orange,
            modifier = Modifier.size(24.dp)
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
