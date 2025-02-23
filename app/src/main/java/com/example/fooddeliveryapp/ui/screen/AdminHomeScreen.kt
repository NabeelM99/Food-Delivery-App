package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberScaffoldState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form state variables
    var burgerName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    // Flavor list state
    val flavors = remember { mutableStateListOf<Map<String, String>>() }
    var newFlavorName by remember { mutableStateOf("") }
    var newFlavorPrice by remember { mutableStateOf("") }
    var newFlavorImgRes by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Burger Details Form
            OutlinedTextField(
                value = burgerName,
                onValueChange = { burgerName = it },
                label = { Text("Burger Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image Resource Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = productDescription,
                onValueChange = { productDescription = it },
                label = { Text("Product Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories (e.g., '650 kcal')") },
                modifier = Modifier.fillMaxWidth()
            )

            // Flavor Section
            Text("Flavors:", style = MaterialTheme.typography.titleMedium)
            flavors.forEach { flavor ->
                Text("- ${flavor["name"]} ($${flavor["price"]})")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newFlavorName,
                    onValueChange = { newFlavorName = it },
                    label = { Text("Flavor Name") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = newFlavorPrice,
                    onValueChange = { newFlavorPrice = it },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = newFlavorImgRes,
                    onValueChange = { newFlavorImgRes = it },
                    label = { Text("Image Resource") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    flavors.add(mapOf(
                        "name" to newFlavorName,
                        "price" to newFlavorPrice,
                        "imgRes" to newFlavorImgRes
                    ))
                    newFlavorName = ""
                    newFlavorPrice = ""
                    newFlavorImgRes = ""
                }) {
                    Icon(Icons.Default.Add, "Add Flavor")
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val burgerData = hashMapOf(
                        "name" to burgerName,
                        "price" to price.toDouble(),
                        "imageUrl" to imageUrl,
                        "productDescription" to productDescription,
                        "nutrition" to hashMapOf(
                            "calories" to hashMapOf(
                                "value" to calories.split(" ")[0],
                                "unit" to calories.split(" ").getOrElse(1) { "" }
                            )
                        ),
                        "flavors" to flavors
                    )

                    db.collection("productdetails")
                        .document("burger${System.currentTimeMillis()}")
                        .set(burgerData)
                        .addOnSuccessListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("Burger added successfully!")
                                // Clear form
                                burgerName = ""
                                price = ""
                                imageUrl = ""
                                productDescription = ""
                                calories = ""
                                flavors.clear()
                            }
                        }
                        .addOnFailureListener { e ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Add Burger to Database")
            }
        }
    }
}