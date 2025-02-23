package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(navController: NavController) {
    var currentScreen by remember { mutableStateOf("main") }
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentScreen != "main") {
                            currentScreen = "main"
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (currentScreen) {
            "main" -> AdminMainMenu(
                onAddProductClick = { currentScreen = "addProduct" },
                onAddDetailsClick = { currentScreen = "addDetails" },
                padding = padding
            )
            "addProduct" -> AddProductForm(
                db = db,
                snackbarHostState = snackbarHostState,
                scope = scope,
                padding = padding,
                onBack = { currentScreen = "main" }
            )
            "addDetails" -> AddProductDetailsForm(
                db = db,
                snackbarHostState = snackbarHostState,
                scope = scope,
                padding = padding,
                onBack = { currentScreen = "main" }
            )
        }
    }
}

@Composable
fun AdminMainMenu(
    onAddProductClick: () -> Unit,
    onAddDetailsClick: () -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onAddProductClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 8.dp)
        ) {
            Text("Add Products")
        }

        Button(
            onClick = onAddDetailsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 8.dp)
        ) {
            Text("Add Product Details")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    padding: PaddingValues,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Burger", "Drink", "Fries", "Pasta", "Juice")
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productId,
            onValueChange = { productId = it },
            label = { Text("Product ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image Resource Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (selectedCategory.isNotEmpty() && productId.isNotEmpty()) {
                    val productData = hashMapOf(
                        "id" to productId.toInt(),
                        "name" to productName,
                        "imageUrl" to imageUrl,
                        "description" to description
                    )

                    db.collection(selectedCategory.lowercase() + "s")
                        .add(productData)
                        .addOnSuccessListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("Product added to $selectedCategory collection!")
                                productId = ""
                                productName = ""
                                imageUrl = ""
                                description = ""
                            }
                        }
                        .addOnFailureListener { e ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Please fill all fields and select category")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Add Product")
        }
    }
}

@Composable
fun AddProductDetailsForm(
    db: FirebaseFirestore,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    padding: PaddingValues,
    onBack: () -> Unit
) {
    var burgerName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    val flavors = remember { mutableStateListOf<Map<String, String>>() }
    var newFlavorName by remember { mutableStateOf("") }
    var newFlavorPrice by remember { mutableStateOf("") }
    var newFlavorImgRes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = burgerName,
            onValueChange = { burgerName = it },
            label = { Text("Product Name") },
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
                    .add(burgerData)
                    .addOnSuccessListener {
                        scope.launch {
                            snackbarHostState.showSnackbar("Product details added successfully!")
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
            Text("Save Details")
        }
    }
}