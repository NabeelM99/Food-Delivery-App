package com.example.fooddeliveryapp.ui.screen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.components.BottomNavBar
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.Red
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// Data class for cart items
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageName: String,
    var quantity: Int = 1
){
    constructor() : this("", "", 0.0, "", 1)
    fun toFirestoreMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "price" to price,
            "imageName" to imageName,
            "quantity" to quantity
        )
    }
}

// ViewModel to manage cart state
class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
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

// Helper function for formatting price (avoid extension function to prevent conflicts)
private fun formatPrice(price: Double, digits: Int): String {
    return "%.${digits}f".format(price)
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

    // Animation states
    val lazyListState = rememberLazyListState()
    var isCheckoutPressed by remember { mutableStateOf(false) }
    val checkoutScale = animateFloatAsState(
        targetValue = if (isCheckoutPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "checkout scale"
    )

    // Animation for TopBar
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Cart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (cartItems.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(28.dp)
                            ) {
                                Text(
                                    text = "${cartItems.sumOf { it.quantity }}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
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
                EmptyCartView(
                    modifier = Modifier.padding(paddingValues),
                    onBrowseClick = { navController.navigate("menu") }
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp), // Space for checkout + nav
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = cartItems,
                        key = { _, item -> item.id }
                    ) { index, cartItem ->
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

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 100,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            CartItemCard(
                                cartItem = cartItem,
                                onIncreaseQuantity = { id ->
                                    scope.launch {
                                        cartViewModel.updateQuantity(id, cartItem.quantity + 1)
                                    }
                                },
                                onDecreaseQuantity = { id ->
                                    scope.launch {
                                        if (cartItem.quantity > 1) {
                                            cartViewModel.updateQuantity(id, cartItem.quantity - 1)
                                        } else {
                                            cartViewModel.removeFromCart(id)
                                        }
                                    }
                                },
                                onRemoveItem = { id ->
                                    scope.launch {
                                        cartViewModel.removeFromCart(id)
                                        snackbarHostState.showSnackbar("Item removed from cart")
                                    }
                                },
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = animatedProgress.value
                                        translationY = (1f - animatedProgress.value) * 50f
                                    }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BottomCheckoutBar(
                        totalPrice = totalPrice,
                        onCheckoutClicked = {
                            isCheckoutPressed = true
                            scope.launch {
                                if (cartItems.isEmpty()) {
                                    snackbarHostState.showSnackbar("Your cart is empty")
                                } else {
                                    delay(100)  // Short delay for button animation
                                    navController.navigate("checkout")
                                }
                                isCheckoutPressed = false
                            }
                        },
                        scale = checkoutScale.value
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
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for swipe to delete
    var offsetX by remember { mutableStateOf(0f) }
    val dismissThreshold = 200f

    var isPressed by remember { mutableStateOf(false) }
    val cardElevation by animateFloatAsState(
        targetValue = if (isPressed) 1f else 6f,
        animationSpec = tween(150),
        label = "card elevation"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "card scale"
    )

    // Quantity animation
    var prevQuantity by remember { mutableIntStateOf(cartItem.quantity) }
    val quantityChange = cartItem.quantity - prevQuantity
    val quantityColor by animateColorAsState(
        targetValue = when {
            quantityChange > 0 -> Orange
            quantityChange < 0 -> Red
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "quantity color"
    )

    val quantityScale by animateFloatAsState(
        targetValue = if (quantityChange != 0) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { prevQuantity = cartItem.quantity },
        label = "quantity scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(cardElevation.dp, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image with gradient overlay
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEEEEE))
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = getDrawableId(cartItem.imageName)),
                    contentDescription = cartItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle: Item Name and Price
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = cartItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${formatPrice(cartItem.price, 2)}",
                    fontSize = 18.sp,
                    color = Orange,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Remove button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onRemoveItem(cartItem.id) },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_minus),
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Remove",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Quantity Selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Plus Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Orange)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Removed rememberRipple
                            onClick = { onIncreaseQuantity(cartItem.id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Increase quantity",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }

                // Quantity Display
                Text(
                    text = "${cartItem.quantity}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = quantityColor,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .graphicsLayer {
                            scaleX = quantityScale
                            scaleY = quantityScale
                        },
                    textAlign = TextAlign.Center
                )

                // Minus Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Removed rememberRipple
                            onClick = { onDecreaseQuantity(cartItem.id) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease quantity",
                        modifier = Modifier.size(16.dp),
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomCheckoutBar(
    totalPrice: Double,
    onCheckoutClicked: () -> Unit,
    scale: Float = 1f
) {
    val totalPriceState = remember { mutableStateOf(totalPrice) }
    val animatedTotalPrice by animateFloatAsState(
        targetValue = totalPrice.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "price animation"
    )

    LaunchedEffect(totalPrice) {
        totalPriceState.value = totalPrice
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)
    ) {
        // Order summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sub-total",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "$${formatPrice(animatedTotalPrice.toDouble(), 2)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Delivery",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "$2.99",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Total amount
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "$${formatPrice(animatedTotalPrice + 2.99, 2)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onCheckoutClicked,
                modifier = Modifier
                    .height(56.dp)
                    .width(160.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
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
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Checkout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier, onBrowseClick: () -> Unit) {
    var isAnimated by remember { mutableStateOf(false) }

    // Animation values
    val containerAlpha by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec = tween(1000),
        label = "container alpha"
    )

    val containerScale by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "container scale"
    )

    val buttonScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        isAnimated = true
        delay(700)
        buttonScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(containerAlpha)
            .graphicsLayer {
                scaleX = containerScale
                scaleY = containerScale
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Empty cart icon with animation
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Empty Cart",
                    tint = Color.LightGray,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your cart is empty",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Add items to your cart to proceed",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBrowseClick,
                modifier = Modifier
                    .height(56.dp)
                    .width(220.dp)
                    .graphicsLayer {
                        scaleX = buttonScale.value
                        scaleY = buttonScale.value
                    }
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
                Text(
                    "Browse Menu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}