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
import com.example.fooddeliveryapp.ui.screen.components.EmptyStateView
import com.example.fooddeliveryapp.ui.screen.components.ErrorView
import com.example.fooddeliveryapp.ui.screen.components.FilterPanel
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.example.fooddeliveryapp.ui.screen.components.SortOptionsBar
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuiceScreen(navController: NavController) {
    val juices = remember { mutableStateListOf<Product>() }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    val selectedOptions = remember { mutableStateListOf<String>() }
    var rating by remember { mutableStateOf(3) }

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

    // Color scheme for juices section
    val primaryColor = Color(0xFFFF6B35) // Warm orange/red for juices
    val secondaryColor = Color(0xFFFFA500) // Classic juice orange
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
        db.collection("juices")
            .get()
            .addOnSuccessListener { result ->
                val fetchedJuices = result.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            type = "juices", // Set collection type
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
                    juices.clear()
                    fetchedJuices.forEachIndexed { index, juice ->
                        delay(50L * index)
                        juices.add(juice)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching juices", e)
                loading = false
                errorMessage = "Failed to load juices: ${e.localizedMessage}"
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
                .padding(start = 10.dp, end = 10.dp)
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
                    .data("https://plus.unsplash.com/premium_photo-1674595249459-53ab1444da88")
                    .crossfade(true)
                    .build(),
                contentDescription = "Juice Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
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
                        text = "JUICES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Chilled, refreshing, and bursting with vitamins",
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
                    //.shadow(4.dp, CircleShape)
                    //.background(Color.White.copy(alpha = 0.8f), CircleShape)
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
                    //.shadow(4.dp, CircleShape)
                    //.background(Color.White.copy(alpha = 0.8f), CircleShape)
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
                    JuiceLoadingAnimation(
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

                juices.isEmpty() -> {
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
                            items = juices,
                            key = { it.id }
                        ) { juice ->
                            AnimatedJuiceCard(
                                juice = juice,
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
                            selectedCategory = selectedCategory,          // Add these
                            onCategoryChange = { selectedCategory = it },
                            selectedOptions = selectedOptions,
                            rating = rating,
                            onRatingChange = { rating = it },
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
fun AnimatedJuiceCard(
    juice: Product,
    navController: NavController,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(50f) }

    LaunchedEffect(juice.id) {
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
                navController.navigate("productDetailsScreen/${juice.type}/${juice.id}")
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
                    painter = painterResource(id = getDrawableId(juice.imageUrl)),
                    contentDescription = juice.name,
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
                    text = juice.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = juice.description,
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
                        text = "$${juice.price}",
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
fun JuiceLoadingAnimation(
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

        // Center juice icon
        Icon(
            painter = painterResource(id = R.drawable.ic_close), // Replace with your juice icon
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









// Animated plate that shows the juice count
@Composable
fun JuiceCountBadge(
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

// JuiceHighlightCard to show featured or popular items with special styling
@Composable
fun JuiceHighlightCard(
    juice: Product,
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
                navController.navigate("productDetailsScreen/${juice.type}/${juice.id}")
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
                        text = juice.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = juice.description,
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
                            text = "$${juice.price} - Add to Cart",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(juice.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = juice.name,
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