package com.example.fooddeliveryapp.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Red
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val address by profileViewModel.address.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Track initial values to detect unsaved changes
    var initialName by remember { mutableStateOf("") }
    var initialMobile by remember { mutableStateOf("") }
    var initialDob by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf(
        userProfile?.mobile?.removePrefix("+973 ") ?: ""
    )}
    val currentAddress = userProfile?.address ?: ""
    var dob by remember { mutableStateOf(userProfile?.dob ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) } // Dialog state
    val selectedAddress = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selectedProfileAddress")





    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    // Sync initial data when userProfile changes
    LaunchedEffect(userProfile) {
        userProfile?.let {
            initialName = it.name
            initialMobile = it.mobile.removePrefix("+973 ")
            initialDob = it.dob
            name = initialName
            mobile = initialMobile
            dob = initialDob
        }
    }

    val hasUnsavedChanges = name != initialName || mobile != initialMobile || dob != initialDob

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    // Dialog for unsaved changes
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("Do you want to save your changes before exiting?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                profileViewModel.updateProfile(name, mobile, address, dob)
                                snackbarHostState.showSnackbar("Profile updated successfully!")
                                navController.popBackStack()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                        showUnsavedDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        navController.popBackStack() // Discard changes
                        showUnsavedDialog = false
                    }
                ) { Text("Discard") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                dob = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Orange, Red)
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Profile",
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (hasUnsavedChanges) {
                                    showUnsavedDialog = true
                                } else {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        floatingActionButton = {
           ExtendedFloatingActionButton(
            onClick = {
                scope.launch {
                    try {
                        profileViewModel.updateProfile(name, mobile, address, dob)
                        snackbarHostState.showSnackbar("Profile updated successfully!")

                        // MAIN THREAD SAFETY
                        withContext(Dispatchers.Main) {
                            navController.popBackStack()
                        }

                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    }
                }
            },
            containerColor = Orange,
            contentColor = Color.White
        ){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = "Save",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = userProfile?.email ?: "",
                onValueChange = {},
                label = { Text("Email (Read-only)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        mobile = it
                    }
                },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                prefix = { Text("+973 ") }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = currentAddress,
                onValueChange = { },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("locationScreen/${"profileEdit"}")
                    },
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Location"
                    )
                }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            )
        }
    }
}