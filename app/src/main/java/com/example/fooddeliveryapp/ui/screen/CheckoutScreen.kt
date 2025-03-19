package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Red
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val cartItems by cartViewModel.cartItems.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val totalPrice = cartItems.sumOf { it.price * it.quantity }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deliveryAddress by remember { mutableStateOf(userProfile?.address ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var expanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cash on Delivery", "Credit Card", "Mobile Payment")

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val addressResult = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("selectedDeliveryAddress", null)
        ?.collectAsState()

    LaunchedEffect(addressResult?.value) {
        addressResult?.value?.let { newAddress ->
            deliveryAddress = newAddress
            // Clear the result to prevent reprocessing
            navController.previousBackStackEntry?.savedStateHandle?.remove<String>("selectedDeliveryAddress")
        }
    }


    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.Observer<String> { newAddress ->
            deliveryAddress = newAddress ?: userProfile?.address ?: ""
        }
        val liveData = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selectedDeliveryAddress")
        liveData?.observeForever(observer)
        onDispose {
            liveData?.removeObserver(observer)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selectedDeliveryAddress")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Checkout", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Order Summary",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(cartItems) { item ->
                OrderItemRow(item = item)
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Delivery Details",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = {  },
                        label = { Text("Delivery Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable{
                                navController.navigate("locationScreen/checkout")
                            },
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Select Location"
                            )
                        }
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            label = { Text("Payment Method") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown,"Show Payment Methods")
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (deliveryAddress.isBlank() || phoneNumber.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all required fields")
                            }
                            return@Button
                        }

                        val firestoreItems = cartItems.map { it.toFirestoreMap() }

                        val order = hashMapOf(
                            "orderId" to UUID.randomUUID().toString(),
                            "userId" to userId,
                            "items" to cartItems,
                            "totalPrice" to totalPrice,
                            "deliveryAddress" to deliveryAddress,
                            "phoneNumber" to phoneNumber,
                            "paymentMethod" to paymentMethod,
                            "timestamp" to Date(),
                            "orderStatus" to "Processing"
                        )

                        db.collection("orders").document(order["orderId"].toString())
                            .set(order)
                            .addOnSuccessListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Order placed successfully!")
                                    cartViewModel.clearCart()
                                    navController.popBackStack()
                                }
                            }
                            .addOnFailureListener { e ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Order failed: ${e.message}")
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Place Order -> $${"%.2f".format(totalPrice)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.quantity}x ${item.name}",
            fontSize = 16.sp
        )
        Text(
            text = "$${"%.2f".format(item.price * item.quantity)}",
            fontSize = 16.sp,
            color = Orange
        )
    }
}