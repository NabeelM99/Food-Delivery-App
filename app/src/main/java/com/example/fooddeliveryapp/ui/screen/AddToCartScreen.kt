package com.example.fooddeliveryapp.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.components.BottomNavBar
import com.example.fooddeliveryapp.ui.theme.Orange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.example.fooddeliveryapp.ui.theme.Red
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class for cart items
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageName: String,
    var quantity: Int = 1
){
    constructor() : this("", "", 0.0, "", 1) // Explicit no-arg constructor
}

//data class CartDocument(val items: List<CartItem> = emptyList())

// ViewModel to manage cart state
class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    //private val auth = FirebaseAuth.getInstance()
    private var userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    // State to hold the list of cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> get() = _cartItems

    init {
        userId?.let { loadCartFromFirestore() } // Auto-load if user is logged in
    }


    // Data class for Firestore serialization
    data class CartData(
        val items: List<CartItem> = emptyList()
    ) {
        constructor() : this(emptyList()) // No-arg constructor for Firestore
    }

    // Initialize when user logs in
    fun initialize(userId: String) {
        this.userId = userId
        loadCartFromFirestore()
    }

    // Save cart to FireStore
    private fun saveCartToFirestore() {
        userId = FirebaseAuth.getInstance().currentUser?.uid // Refresh UID
        userId?.let { uid ->
            val cartData = CartData(_cartItems.value)
            db.collection("carts").document(uid)
                .set(cartData)
                .addOnSuccessListener {
                    println("DEBUG: Cart saved for UID: $uid")
                }
                .addOnFailureListener { e ->
                    println("DEBUG: Firestore write error: ${e.message}")
                }
        } ?: run {
            println("DEBUG: User ID is null. Cannot save cart.")
        }
    }



    // Add an item to the cart
    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            val existingItem = _cartItems.value.find { it.id == item.id }
            val newList = if (existingItem != null) {
                _cartItems.value.map {
                    if (it.id == item.id) it.copy(quantity = it.quantity + item.quantity)
                    else it
                }
            } else {
                _cartItems.value + item
            }
            _cartItems.value = newList
            saveCartToFirestore() // Explicit save
        }
    }

    // Load cart from Firestore
    private fun loadCartFromFirestore() {
        userId?.let { uid ->
            db.collection("carts").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    val items = snapshot?.toObject(CartData::class.java)?.items ?: emptyList()
                    _cartItems.value = items
                }
        }
    }




    // Remove an item from the cart
    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _cartItems.value = _cartItems.value.filter { it.id != itemId }
            saveCartToFirestore()
        }
    }

    // Update the quantity of an item in the cart
    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            _cartItems.value = _cartItems.value.map {
                if (it.id == itemId) it.copy(quantity = quantity) else it
            }
            saveCartToFirestore()
        }
    }
    fun clearCart() {
        _cartItems.value = emptyList()
        userId = null
        userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            db.collection("carts").document(uid).delete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCartScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel() // Inject the CartViewModel
) {
    // Observe the cart items from the ViewModel
    val cartItems by cartViewModel.cartItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val totalPrice = cartItems.sumOf { it.price * it.quantity }
    var currentRoute by remember { mutableStateOf("cart") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Cart",  color = Color.White,
                    fontWeight = FontWeight.Bold
                    //modifier = Modifier.padding(start = 16.dp)
                ) },

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
                    containerColor = Color(0xFFFFA500)
                )
            )
        },
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
            if (cartItems.isEmpty()) {
                EmptyCartView(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp), // Space for checkout + nav
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems) { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            onIncreaseQuantity = { id ->
                                cartViewModel.updateQuantity(id, cartItem.quantity + 1)
                            },
                            onDecreaseQuantity = { id ->
                                if (cartItem.quantity > 1) {
                                    cartViewModel.updateQuantity(id, cartItem.quantity - 1)
                                } else {
                                    cartViewModel.removeFromCart(id)
                                }
                            },
                            onRemoveItem = { id ->
                                cartViewModel.removeFromCart(id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Item removed from cart")
                                }
                            }
                        )
                    }
                }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    BottomCheckoutBar(
                        totalPrice = totalPrice,
                        onCheckoutClicked = {
                            scope.launch {
                                if (cartItems.isEmpty()) {
                                    snackbarHostState.showSnackbar("Your cart is empty")
                                } else {
                                    snackbarHostState.showSnackbar("Proceeding to checkout")
                                    // Here you would navigate to checkout screen
                                     navController.navigate("checkout")
                                }
                            }
                        },
                    )
                }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = getDrawableId(cartItem.imageName)),
                    contentDescription = cartItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle: Item Name and Price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${cartItem.price.format(2)}",
                    fontSize = 18.sp,
                    color = Orange,
                )
            }

            // Right: Quantity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Minus Button
                IconButton(
                    onClick = {
                        if (cartItem.quantity > 1) {
                            onDecreaseQuantity(cartItem.id)
                        } else {
                            onRemoveItem(cartItem.id)
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .padding(4.dp)
                        .background(Color.White),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease quantity",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Red
                    )
                }

                // Quantity Display
                Text(
                    text = "${cartItem.quantity}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                // Plus Button
                IconButton(
                    onClick = { onIncreaseQuantity(cartItem.id) },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .padding(4.dp)
                        .background(Orange)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Increase quantity",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BottomCheckoutBar(
    totalPrice: Double,
    onCheckoutClicked: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        color = MaterialTheme.colorScheme.surface,
        //shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
               // .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total:",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${totalPrice.format(2)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onCheckoutClicked,
                modifier = Modifier
                    .height(50.dp)
                    .width(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Orange, Red)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Checkout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your cart is empty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add items to your cart to proceed",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Navigate to menu */ },
                modifier = Modifier
                    .height(48.dp)
                    .width(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                )
            ) {
                Text("Browse Menu", fontWeight = FontWeight.Bold)
            }

        }
    }
}