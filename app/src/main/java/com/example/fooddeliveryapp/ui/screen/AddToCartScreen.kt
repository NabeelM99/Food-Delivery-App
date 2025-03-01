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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // For viewModelScope
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.home_components.menusection_components.getImageResourceId
import com.example.fooddeliveryapp.ui.theme.Orange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Data class for cart items
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageName: String,
    var quantity: Int = 1
)

// ViewModel to manage cart state
class CartViewModel : ViewModel() {
    // State to hold the list of cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> get() = _cartItems

    // Add an item to the cart
    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            Log.d("CartViewModel", "Adding item to cart: ${item.name} (ID: ${item.id})")
            val existingItem = _cartItems.value.find { it.id == item.id }
            if (existingItem != null) {
                Log.d("CartViewModel", "Updating quantity for item: ${item.name}")
                _cartItems.value = _cartItems.value.map {
                    if (it.id == item.id) it.copy(quantity = it.quantity + item.quantity) else it
                }
            } else {
                Log.d("CartViewModel", "Adding new item: ${item.name}")
                _cartItems.value = _cartItems.value + item
            }
            Log.d("CartViewModel", "Cart items: ${_cartItems.value}")
        }
    }

    // Remove an item from the cart
    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _cartItems.value = _cartItems.value.filter { it.id != itemId }
        }
    }

    // Update the quantity of an item in the cart
    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            _cartItems.value = _cartItems.value.map {
                if (it.id == itemId) it.copy(quantity = quantity) else it
            }
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

    // Calculate the total price
    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomCheckoutBar(
                totalPrice = totalPrice,
                onCheckoutClicked = {
                    scope.launch {
                        if (cartItems.isEmpty()) {
                            snackbarHostState.showSnackbar("Your cart is empty")
                        } else {
                            snackbarHostState.showSnackbar("Proceeding to checkout")
                            // Here you would navigate to checkout screen
                            // navController.navigate("checkout")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
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
            // Left: Item Image
            Image(
                painter = painterResource(id = getImageResourceId(cartItem.imageName)),
                contentDescription = cartItem.name,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

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
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
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
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total:",
                    fontSize = 14.sp,
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
                    .height(48.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Browse Menu")
            }

        }
    }
}

fun getImageResourceId(imageName: String): Int {
    return when (imageName) {
        "img_classiccheeseburger" -> R.drawable.img_classiccheeseburger
        "img_doubleburger" -> R.drawable.img_doubleburger
        "img_chickenburger" -> R.drawable.img_chickenburger
        "img_veggieburger" -> R.drawable.img_veggieburger
        "img_beefburger" -> R.drawable.img_beefburger
        "img_orangejuice" -> R.drawable.img_orangejuice
        "img_mangojuice" -> R.drawable.img_mangojuice
        "img_avocadojuice" -> R.drawable.img_avocadojuice
        "img_pineapplejuice" -> R.drawable.img_pineapplejuice
        "img_papayajuice" -> R.drawable.img_papayajuice
        "img_watermelonjuice" -> R.drawable.img_watermelonjuice
        "img_pimiento" -> R.drawable.img_pimiento
        "img_potatotornado" -> R.drawable.img_potatotornado
        "img_sweetpotatofries" -> R.drawable.img_sweetpotatofries
        "img_thricefries" -> R.drawable.img_thricefries
        "img_wedgecutfries" -> R.drawable.img_wedgecutfries
        "img_bbqchickenpasta" -> R.drawable.img_bbqchickenpasta
        "img_tomatoandgarlicpasta" -> R.drawable.img_tomatoandgarlicpasta
        "img_pastaalavodka" -> R.drawable.img_pastaalavodka
        "img_chickpeapasta" -> R.drawable.img_chickpeapasta
        "img_fettuccinealfredopasta" -> R.drawable.img_fettuccinealfredopasta
        "img_greenpasta" -> R.drawable.img_greenpasta
        "img_lemonpasta" -> R.drawable.img_lemonpasta
        else -> R.drawable.img_placeholder // Fallback image
    }
}