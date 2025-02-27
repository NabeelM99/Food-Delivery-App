package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val email = auth.currentUser?.email ?: ""

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedProfileImage by remember { mutableStateOf("img_profile_placeholder") }
    var showImageSelector by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    firstName = document.getString("firstName") ?: ""
                    lastName = document.getString("lastName") ?: ""
                    phoneNumber = document.getString("phoneNumber") ?: ""
                    selectedProfileImage = document.getString("profileImage") ?: "img_profile_placeholder"
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Image(
                    painter = painterResource(id = getProfileImageResource(selectedProfileImage)),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { showImageSelector = true },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Change Photo"
                    )
                }
            }

            if (showImageSelector) {
                ImageSelectorDialog(
                    onImageSelected = { imageName ->
                        selectedProfileImage = imageName
                        showImageSelector = false
                    },
                    onDismiss = { showImageSelector = false }
                )
            }

            // Rest of the form fields...

            Button(
                onClick = {
                    val userData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phoneNumber" to phoneNumber,
                        "profileImage" to selectedProfileImage,
                        "email" to email
                    )

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Composable
fun ImageSelectorDialog(onImageSelected: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Profile Picture") },
        text = {
            Column {
                listOf("img_profile1", "img_profile2", "img_profile3").forEach { imageName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onImageSelected(imageName) }
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = getProfileImageResource(imageName)),
                            contentDescription = "Profile Option",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = imageName.replace("img_", "").replace("_", " ").capitalize())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}