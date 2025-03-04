package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load existing data
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    doc.getString("name")?.let { name = it }
                    doc.getString("dob")?.let { dob = it }
                    doc.getString("nationality")?.let { nationality = it }
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    currentUser?.uid?.let { uid ->
                        val updates = hashMapOf<String, Any>(
                            "name" to name,
                            "dob" to dob,
                            "nationality" to nationality
                        )

                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .update(updates)
                            .addOnSuccessListener {
                                scope.launch {
                                    // Show success message
                                    snackbarHostState.showSnackbar("Changes saved successfully!")
                                    // Navigate back after message is shown
                                    delay(1000) // Optional delay
                                    navController.popBackStack()
                                }
                            }
                            .addOnFailureListener { e ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Failed to save: ${e.message}")
                                }
                            }
                    }
                },
                text = { Text("Save Changes") },
                icon = { Icon(Icons.Default.ShoppingCart, "Save") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality") },
                modifier = Modifier.fillMaxWidth()
            )

            // Read-only email
            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }
    }
}