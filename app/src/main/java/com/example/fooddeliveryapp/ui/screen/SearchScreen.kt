package com.example.fooddeliveryapp.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.SearchViewModel
import com.example.fooddeliveryapp.ui.screen.components.ProductCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val searchViewModel: SearchViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Collect StateFlow values properly
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val error by searchViewModel.error.collectAsState()

    // Animation states
    val searchBarExpanded = remember { mutableStateOf(false) }
    val emptyStateAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        searchBarExpanded.value = true
        delay(100)
        focusRequester.requestFocus()
    }

    LaunchedEffect(searchQuery) {
        delay(400)
        searchViewModel.searchProducts(searchQuery)
        emptyStateAlpha.snapTo(0f)
        emptyStateAlpha.animateTo(1f, animationSpec = tween(300))
    }

    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {},
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Animated Search Bar
            AnimatedVisibility(
                visible = searchBarExpanded.value,
                enter = expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text("Search foods and drinks...",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    coroutineScope.launch {
                                        focusRequester.requestFocus()
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search filters chip section
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty() && searchResults.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                // This could be populated with category filters in a real app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { /* Filter logic */ },
                        label = { Text("All") },
                        modifier = Modifier.animateEnterExit(
                            enter = slideInHorizontally { with(density) { -40.dp.roundToPx() } } + fadeIn(),
                            exit = slideOutHorizontally { with(density) { -40.dp.roundToPx() } } + fadeOut()
                        )
                    )

                    FilterChip(
                        selected = false,
                        onClick = { /* Filter logic */ },
                        label = { Text("Foods") },
                        modifier = Modifier.animateEnterExit(
                            enter = slideInHorizontally { with(density) { -40.dp.roundToPx() } } + fadeIn(
                                initialAlpha = 0f,
                                animationSpec = tween(durationMillis = 300, delayMillis = 100)
                            ),
                            exit = slideOutHorizontally { with(density) { -40.dp.roundToPx() } } + fadeOut()
                        )
                    )

                    FilterChip(
                        selected = false,
                        onClick = { /* Filter logic */ },
                        label = { Text("Drinks") },
                        modifier = Modifier.animateEnterExit(
                            enter = slideInHorizontally { with(density) { -40.dp.roundToPx() } } + fadeIn(
                                initialAlpha = 0f,
                                animationSpec = tween(durationMillis = 300, delayMillis = 200)
                            ),
                            exit = slideOutHorizontally { with(density) { -40.dp.roundToPx() } } + fadeOut()
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
            ) {
                when {
                    isLoading -> {
                        LoadingAnimation(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    error != null -> {
                        ErrorStateAnimation(
                            errorMessage = error!!,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    searchResults.isEmpty() -> {
                        EmptyStateAnimation(
                            message = if (searchQuery.isEmpty()) "Start typing to search"
                            else "No results found for \"$searchQuery\"",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .graphicsLayer { alpha = emptyStateAlpha.value }
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                items = searchResults,
                                key = { it.id }
                            ) { product ->
                                AnimatedProductCard(
                                    product = product,
                                    productType = product.type,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedProductCard(
    product: com.example.fooddeliveryapp.ui.screen.components.Product,
    productType: String,
    navController: NavController
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(product.id) {
        visible = false
        delay(100)  // Stagger the animation
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically() + fadeOut()
    ) {
        ProductCard(
            product = product,
            productType = productType,
            navController = navController
        )
    }
}

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val size by infiniteTransition.animateFloat(
        initialValue = 48f,
        targetValue = 64f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotate"
    )

    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(size.dp)
                .graphicsLayer {
                    rotationZ = rotation
                },
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorStateAnimation(errorMessage: String, modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(errorMessage) {
        startAnimation = true
        if (startAnimation) {
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    0f at 0
                    -10f at 100
                    10f at 200
                    -8f at 300
                    8f at 400
                    -5f at 500
                    0f at 600
                }
            )
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .graphicsLayer { translationX = shake.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun EmptyStateAnimation(message: String, modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(message) {
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
            .padding(32.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            ) {
                label()
            }
        }
    }
}