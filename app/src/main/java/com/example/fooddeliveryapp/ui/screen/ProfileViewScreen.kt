package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fooddeliveryapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userData by remember { mutableStateOf<UserProfile?>(null) }

    // Fetch user data from Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val docRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            val listener = docRef.addSnapshotListener { doc, _ ->
                userData = doc?.toObject(UserProfile::class.java)
            }
        }
    }

    DisposableEffect(currentUser?.uid) {
        val listener = currentUser?.uid?.let { uid ->
            val docRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            val listenerRegistration = docRef.addSnapshotListener { doc, _ ->
                userData = doc?.toObject(UserProfile::class.java)
            }
            listenerRegistration // Return the listener for cleanup
        }

        // Cleanup listener when the composable is disposed
        onDispose {
            listener?.remove() // Remove the listener
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("profileEdit") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, "Edit Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            AsyncImage(
                model = userData?.profilePicture ?: R.drawable.img_profile1,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User Details
            ProfileDetailItem("Name", userData?.name ?: "Not set")
            ProfileDetailItem("Email", currentUser?.email ?: "No email")
            ProfileDetailItem("Date of Birth", userData?.dob ?: "Not set")
            ProfileDetailItem("Nationality", userData?.nationality ?: "Not set")
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Data class for Firestore
data class UserProfile(
    val name: String = "",
    val dob: String = "",
    val nationality: String = "",
    val profilePicture: String = ""
)