package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurgerScreen(navController: NavController) {
    val burgers = remember { mutableStateListOf<Product>() }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val headerHeight = 200.dp
    val headerOffsetHeightPx = remember { mutableStateOf(0f) }
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val headerBgAlpha = remember { Animatable(1f) }

    // UI State variables
    var showSortOptions by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf("Popular") }
    var showFilters by remember { mutableStateOf(false) }
    var priceRange by remember { mutableStateOf(0f..50f) }

    // Color scheme for burgers section
    val primaryColor = Color(0xFFFF6B35) // Warm orange/red for burgers
    val secondaryColor = Color(0xFFFFA500) // Classic burger orange
    val accentColor = Color(0xFF4CAF50) // Green for "fresh" elements
    val bgGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.1f),
                Color.White
            )
        )
    }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("burgers")
            .get()
            .addOnSuccessListener { result ->
                val fetchedBurgers = result.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            type = "burgers", // Set collection type
                            productDescription = doc.getString("productDescription") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing document: ${doc.id}", e)
                        null
                    }
                }

                // Simulate staggered data loading for visual effect
                coroutineScope.launch {
                    loading = false
                    delay(100)
                    burgers.clear()
                    fetchedBurgers.forEachIndexed { index, burger ->
                        delay(50L * index)
                        burgers.add(burger)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching burgers", e)
                loading = false
                errorMessage = "Failed to load burgers: ${e.localizedMessage}"
            }
    }

    // Observer for scroll state
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { index ->
                val scrollOffset = if (index > 0) {
                    headerHeightPx
                } else {
                    scrollState.firstVisibleItemScrollOffset.toFloat().coerceIn(0f, headerHeightPx)
                }

                headerOffsetHeightPx.value = scrollOffset

                // Animate header background alpha based on scroll
                val targetAlpha = 1f - (scrollOffset / headerHeightPx).coerceIn(0f, 1f)
                headerBgAlpha.animateTo(
                    targetValue = targetAlpha,
                    animationSpec = tween(150)
                )
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Animated header image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .graphicsLayer {
                    alpha = headerBgAlpha.value
                    // Parallax effect
                    translationY = -headerOffsetHeightPx.value * 0.5f
                }
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .zIndex(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://images.unsplash.com/photo-1561758033-d89a9ad46330")
                    .crossfade(true)
                    .build(),
                contentDescription = "Burger Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            // Dark overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Header content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "BURGERS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Premium beef, fresh ingredients",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Top Bar
        CenterAlignedTopAppBar(
            title = { /* Empty title */ },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filterlist),
                        contentDescription = "Filter",
                        tint = primaryColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = primaryColor
            ),
            modifier = Modifier.zIndex(2f)
        )

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            when {
                loading -> {
                    BurgerLoadingAnimation(
                        modifier = Modifier.align(Alignment.Center),
                        primaryColor = primaryColor
                    )
                }

                errorMessage != null -> {
                    ErrorView(
                        message = errorMessage!!,
                        onRetry = {
                            errorMessage = null
                            loading = true
                            // Reload data here
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                burgers.isEmpty() -> {
                    EmptyStateView(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Sort options bar - appears when user scrolls past header
                    AnimatedVisibility(
                        visible = headerOffsetHeightPx.value > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(3f)
                    ) {
                        SortOptionsBar(
                            selectedOption = selectedSortOption,
                            onOptionSelected = { selectedSortOption = it },
                            primaryColor = primaryColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .shadow(4.dp)
                        )
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 0.dp),
                        contentPadding = PaddingValues(
                            top = headerHeight + 16.dp,
                            bottom = 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = burgers,
                            key = { it.id }
                        ) { burger ->
                            AnimatedBurgerCard(
                                burger = burger,
                                navController = navController,
                                primaryColor = primaryColor,
                                accentColor = accentColor
                            )
                        }
                    }

                    // Filter drawer
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp)
                            .align(Alignment.CenterEnd)
                            .zIndex(5f)
                    ) {
                        FilterPanel(
                            priceRange = priceRange,
                            onPriceRangeChange = { priceRange = it },
                            onClose = { showFilters = false },
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }

                    // Scrim for filter drawer
                    if (showFilters) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        showFilters = false
                                    }
                                }
                                .zIndex(4f)
                        )
                    }

                    // FAB for scroll to top
                    AnimatedVisibility(
                        visible = scrollState.firstVisibleItemIndex > 5,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .zIndex(3f)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(0)
                                }
                            },
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Scroll to top"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBurgerCard(
    burger: Product,
    navController: NavController,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(50f) }

    LaunchedEffect(burger.id) {
        if (!animationPlayed) {
            launch { scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow)) }
            launch { alpha.animateTo(1f, animationSpec = tween(300)) }
            launch { offsetY.animateTo(0f, animationSpec = tween(300)) }
            animationPlayed = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = offsetY.value
                this.alpha = alpha.value
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = primaryColor.copy(alpha = 0.1f)
            )
            .clickable {
                navController.navigate("productDetailsScreen/${burger.type}/${burger.id}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background pattern
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                // Product image
                Image(
                    painter = painterResource(id = getDrawableId(burger.imageUrl)),
                    contentDescription = burger.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                // Sale tag if applicable
                if (Random.nextFloat() < 0.3f) { // Just for demo, 30% chance to show sale tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(accentColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SALE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = burger.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = burger.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Text(
                        text = "$${burger.price}",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )

                    // Add button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor)
                            .clickable {
                                // Handle add to cart
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BurgerLoadingAnimation(
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // Multiple pulsing circles
    val circles = 3
    val delays = remember { List(circles) { it * 300 } }
    val animations = delays.map { delay ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = delay),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse$delay"
        )
    }

    // Loading text
    val loadingDots = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing circles
        animations.forEachIndexed { index, anim ->
            val animValue = anim.value

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = 0.2f + (animValue * 0.8f)
                        scaleY = 0.2f + (animValue * 0.8f)
                        alpha = 1f - animValue
                    }
                    .background(
                        color = primaryColor.copy(alpha = 0.3f - (animValue * 0.3f)),
                        shape = CircleShape
                    )
            )
        }

        // Center burger icon
        Icon(
            painter = painterResource(id = R.drawable.ic_close), // Replace with your burger icon
            contentDescription = "Loading",
            tint = primaryColor,
            modifier = Modifier.size(48.dp)
        )

        // Loading text
        Text(
            text = "Loading" + ".".repeat(loadingDots.value.toInt()),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = primaryColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(message) {
        startAnimation = true
        if (startAnimation) {
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 1000
                    0f at 0
                    -10f at 100
                    10f at 300
                    -8f at 500
                    8f at 700
                    0f at 1000
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .graphicsLayer { translationX = shake.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = "Error",
            tint = Color.Red.copy(alpha = 0.8f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA500)
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        startAnimation = true
        if (startAnimation) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_nofood),
            contentDescription = "No burgers",
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No burgers available",
            style = MaterialTheme.typography.titleLarge,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Come back later for more delicious options",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SortOptionsBar(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val options = listOf("Popular", "Price: Low to High", "Price: High to Low", "Rating")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) primaryColor.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) primaryColor else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) primaryColor else Color.DarkGray,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onClose: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Beef", "Chicken", "Vegetarian", "Spicy")
    var selectedCategory by remember { mutableStateOf("All") }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .shadow(8.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.LightGray
            )

            // Category filters
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price range filter
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            RangeSlider(
                value = priceRange,
                onValueChange = onPriceRangeChange,
                valueRange = 0f..50f,
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = primaryColor.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Additional filters
            Text(
                text = "Dietary Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Checkbox filters
            val dietaryOptions = listOf("Vegetarian", "Vegan", "Gluten-Free", "Halal")
            val selectedOptions = remember { mutableStateListOf<String>() }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dietaryOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedOptions.contains(option)) {
                                    selectedOptions.remove(option)
                                } else {
                                    selectedOptions.add(option)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedOptions.contains(option),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedOptions.add(option)
                                } else {
                                    selectedOptions.remove(option)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryColor,
                                uncheckedColor = Color.Gray
                            )
                        )

                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating filter
            Text(
                text = "Minimum Rating",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Star rating selector
            var rating by remember { mutableStateOf(3) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..5) {
                    Icon(
                        painter = if (i <= rating)
                            painterResource(R.drawable.ic_star)  // Default filled star
                        else
                            painterResource(R.drawable.ic_starborder),  // Your custom outline star
                        contentDescription = "Star $i",
                        tint = if (i <= rating) secondaryColor else Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { rating = i }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Apply filters button
            Button(
                onClick = {
                    // Apply filters logic
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Apply Filters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset filters button
            OutlinedButton(
                onClick = {
                    // Reset filters logic
                    selectedCategory = "All"
                    onPriceRangeChange(0f..50f)
                    selectedOptions.clear()
                    rating = 3
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryColor
                ),
                border = BorderStroke(1.dp, primaryColor),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Reset Filters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Animated plate that shows the burger count
@Composable
fun BurgerCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val animatedScale = remember { Animatable(0.8f) }
    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(count) {
        // Animate the badge when count changes
        animatedScale.snapTo(0.8f)
        animatedRotation.snapTo(-10f)

        launch {
            animatedScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            animatedRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(300)
            )
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale.value
                scaleY = animatedScale.value
                rotationZ = animatedRotation.value
            }
            .shadow(4.dp, CircleShape)
            .background(Color(0xFFFF6B35), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

// BurgerHighlightCard to show featured or popular items with special styling
@Composable
fun BurgerHighlightCard(
    burger: Product,
    navController: NavController,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFF6B35)
) {
    val animOffset = remember { Animatable(30f) }
    val animAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { animOffset.animateTo(0f, animationSpec = tween(500)) }
        launch { animAlpha.animateTo(1f, animationSpec = tween(500)) }
    }

    // Create the shimmer effect
    val gradient = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.9f),
            primaryColor.copy(alpha = 0.3f),
            primaryColor.copy(alpha = 0.9f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                alpha = animAlpha.value
                translationY = animOffset.value
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                navController.navigate("productDetailsScreen/${burger.type}/${burger.id}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.9f),
                                primaryColor.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            // Content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Text content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = "FEATURED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = burger.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = burger.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { /* Add to cart logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = primaryColor
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "$${burger.price} - Add to Cart",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(burger.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = burger.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(8.dp)
                )
            }

            // "Hot" badge if needed
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .drawBehind {
                        // Draw a star shape or use a simple circle
                        drawCircle(
                            color = Color.Yellow,
                            radius = 24.dp.toPx()
                        )
                    }
                    .padding(8.dp)
            ) {
                Text(
                    text = "HOT",
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Extension for the IconButton with animation
@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .background(Color.White, CircleShape)
            .shadow(4.dp, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    color = tint
                ),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}






/*My Old Code for BurgerScreen:
package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.example.fooddeliveryapp.ui.screen.components.ProductCard
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurgerScreen(navController: NavController) {
    val burgers = remember { mutableStateListOf<Product>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("burgers")
            .get()
            .addOnSuccessListener { result ->
                val fetchedBurgers = result.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            type = "burgers", // Set collection type
                            productDescription = doc.getString("productDescription") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing document: ${doc.id}", e)
                        null
                    }
                }
                burgers.clear()
                burgers.addAll(fetchedBurgers)
                loading = false
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching burgers", e)
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Burgers") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA500)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(burgers) { burger ->
                //BurgerCard(burger = burger, navController = navController)
                ProductCard(
                    product = burger,
                    productType = "burgers",
                    navController = navController
                )
            }
        }
    }
}
 */