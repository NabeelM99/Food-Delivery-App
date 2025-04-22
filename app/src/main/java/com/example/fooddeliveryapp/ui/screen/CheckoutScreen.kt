package com.example.fooddeliveryapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.R
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
    var deliveryAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var expanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cash on Delivery", "Credit Card", "Mobile Payment")

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedAddress = savedStateHandle?.get<String>("selectedDeliveryAddress")

    LaunchedEffect(selectedAddress) {
        selectedAddress?.let {
            deliveryAddress = it
        }
    }

    LaunchedEffect(userProfile) {
        if (deliveryAddress.isEmpty()) {
            userProfile?.address?.let { deliveryAddress = it }
        }
    }

    // Animation for the order button
    var isOrderButtonPressed by remember { mutableStateOf(false) }
    val orderButtonScale by animateFloatAsState(
        targetValue = if (isOrderButtonPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "order button scale"
    )

    // Animation for the top bar
    val lazyListState = rememberLazyListState()
    val topBarElevation by animateFloatAsState(
        targetValue = if (lazyListState.firstVisibleItemIndex > 0) 8f else 0f,
        animationSpec = tween(300),
        label = "topBar elevation"
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .shadow(8.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    action = {
                        TextButton(onClick = { data.dismiss() }) {
                            Text("OK", color = Orange)
                        }
                    }
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Checkout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Orange
                ),
                modifier = Modifier.shadow(topBarElevation.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
                    )
                )
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Order Summary",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider(color = Color.LightGray, thickness = 1.dp)
                            }
                        }
                    }
                }

                itemsIndexed(cartItems) { index, item ->
                    val animatedProgress = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        animatedProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    EnhancedOrderItemRow(
                        item = item,
                        modifier = Modifier
                            .scale(animatedProgress.value)
                            .alpha(animatedProgress.value)
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Delivery Details",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = deliveryAddress,
                                    onValueChange = { },
                                    label = {
                                        Text(
                                            "Delivery Address",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                        .clickable {
                                            navController.navigate("locationScreen/checkout")
                                        },
                                    enabled = false,
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Orange.copy(alpha = 0.1f))
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Select Location",
                                                tint = Orange
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color.LightGray,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color(0xFFF5F5F5)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = {
                                        Text(
                                            "Phone Number",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Phone
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Orange,
                                        cursorColor = Orange
                                    )
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = paymentMethod,
                                        onValueChange = {},
                                        label = {
                                            Text(
                                                "Payment Method",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                                        readOnly = true,
                                        trailingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Orange.copy(alpha = 0.1f))
                                                    .padding(8.dp)
                                                    .clickable { expanded = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_creditcard),
                                                    contentDescription = "Select Payment Method",
                                                    tint = Orange
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            disabledBorderColor = Color.LightGray
                                        )
                                    )

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        paymentMethods.forEach { method ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        method,
                                                        fontWeight = if (method == paymentMethod)
                                                            FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    paymentMethod = method
                                                    expanded = false
                                                },
                                                colors = MenuDefaults.itemColors(
                                                    textColor = if (method == paymentMethod)
                                                        Orange else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", totalPrice)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Orange
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        isOrderButtonPressed = true
                                        if (deliveryAddress.isBlank() || phoneNumber.isBlank()) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please fill all required fields")
                                                isOrderButtonPressed = false
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
                                                isOrderButtonPressed = false
                                            }
                                            .addOnFailureListener { e ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Order failed: ${e.message}")
                                                }
                                                isOrderButtonPressed = false
                                            }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .scale(orderButtonScale)
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Orange, Red)
                                            )
                                        ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 8.dp,
                                        pressedElevation = 2.dp
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_plus), // Change this to an appropriate icon
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Place Order • $${String.format("%.2f", totalPrice)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add some space at the bottom
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun EnhancedOrderItemRow(item: CartItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Orange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "$${String.format("%.2f", item.price * item.quantity)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Orange
            )
        }
    }
}