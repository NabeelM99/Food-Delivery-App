package com.example.fooddeliveryapp.ui.screen

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await







@Composable
fun ProductDetailsScreen(
    productType: String,
    productId: String,
    navController: NavController,
    cartViewModel: CartViewModel = viewModel()
) {
    val primaryColor = Color(0xFFFF6B35)
    val secondaryColor = Color(0xFFFFA500)
    val accentColor = Color(0xFF4CAF50)
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.1f),
            Color.White
        )
    )

    val db = FirebaseFirestore.getInstance()
    var productDetails by remember { mutableStateOf<ProductDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Animation states
    val headerHeight = 300.dp
    val headerOffsetHeightPx = remember { mutableStateOf(0f) }
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val headerBgAlpha = remember { Animatable(1f) }
    val animatedScale = remember { Animatable(1f) }

    // Add button animation
    val addButtonScale = remember { Animatable(1f) }
    val selectedFlavorIndex = remember { mutableStateOf(-1) }

    LaunchedEffect(productType, productId) {
        try {
            val docRef = db.collection("productdetails").document(productId)
            val document = docRef.get().await()
            Log.d("Firestore", "Document exists: ${document.exists()}")
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    Log.d("Firestore", "Document data: $data")
                    val nutritionData = data["nutrition"] as? Map<String, Any> ?: emptyMap()
                    val flavorsData = data["flavors"] as? List<Map<String, Any>> ?: emptyList()

                    productDetails = ProductDetails(
                        id = (data["id"] as? Long)?.toString() ?: "",
                        name = data["name"] as? String ?: "",
                        price = data["price"] as? Double ?: 0.0,
                        imageUrl = data["imageUrl"] as? String ?: "",
                        productDescription = data["productDescription"] as? String ?: "",
                        nutrition = ProductNutritionState(
                            calories = Calories(
                                value = (nutritionData["calories"] as? Map<String, String>)?.get("value") ?: "",
                                unit = (nutritionData["calories"] as? Map<String, String>)?.get("unit") ?: ""
                            ),
                            nutrition = (nutritionData["nutrition"] as? List<Map<String, Any>> ?: emptyList()).map {
                                NutritionState(
                                    amount = it["amount"] as? String ?: "",
                                    unit = it["unit"] as? String ?: "",
                                    title = it["title"] as? String ?: ""
                                )
                            }
                        ),
                        flavors = flavorsData.map {
                            ProductFlavorState(
                                name = it["name"] as? String ?: "",
                                price = when (val p = it["price"]) {
                                    is Number -> p.toDouble()
                                    is String -> p.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                },
                                imgRes = it["imgRes"] as? String ?: ""
                            )
                        }
                    )
                }
            } else {
                Log.e("Firestore", "Document does not exist for productId: $productId")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching product details", e)
        } finally {
            isLoading = false
        }
    }

    // Animation for scroll effect
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemScrollOffset }
            .collect { offset ->
                headerOffsetHeightPx.value = offset.toFloat().coerceIn(0f, headerHeightPx)
                val alpha = 1f - (offset / headerHeightPx).coerceIn(0f, 1f)
                headerBgAlpha.animateTo(alpha)

                // Scale the image based on scroll
                val scale = 1f - (offset / headerHeightPx).coerceIn(0f, 0.25f)
                animatedScale.animateTo(scale)
            }
    }

    if (isLoading) {
        LoadingScreen()
        return
    }

    if (productDetails == null) {
        ProductNotFoundScreen()
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isLoading && productDetails != null) {
                OrderActionBar(
                    state = OrderState(
                        amount = amount,
                        totalPrice = "$${productDetails?.price?.times(amount)?.format(2) ?: "0.00"}"
                    ),
                    onAddItemClicked = {
                        scope.launch {
                            addButtonScale.animateTo(0.8f,
                                animationSpec = tween(100, easing = LinearEasing)
                            )
                            addButtonScale.animateTo(1f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                            amount++
                        }
                    },
                    onRemoveItemClicked = {
                        if (amount > 1) {
                            scope.launch {
                                addButtonScale.animateTo(0.8f,
                                    animationSpec = tween(100, easing = LinearEasing)
                                )
                                addButtonScale.animateTo(1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                                amount--
                            }
                        }
                    },
                    onCheckOutClicked = {
                        scope.launch {
                            addButtonScale.animateTo(0.8f,
                                animationSpec = tween(100, easing = LinearEasing)
                            )
                            addButtonScale.animateTo(1f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )

                            val item = CartItem(
                                id = productId,
                                name = productDetails?.name ?: "",
                                price = productDetails?.price ?: 0.0,
                                imageName = productDetails?.imageUrl ?: "",
                                quantity = amount
                            )
                            cartViewModel.addToCart(item)

                            snackbarHostState.showSnackbar("Item is added to the cart")
                        }
                    },
                    buttonScale = addButtonScale.value
                )
            }
        }
    ) { paddingValues ->
        productDetails?.let { details ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(brush = bgGradient)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    ProductPreviewSection(
                        productType = productType,
                        productId = productId,
                        navController = navController,
                        productDetails = details,
                        animatedScale = animatedScale.value,
                        headerBgAlpha = headerBgAlpha.value
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FlavorSection(
                        data = details.flavors,
                        selectedIndex = selectedFlavorIndex.value,
                        onFlavorSelected = { index ->
                            scope.launch {
                                selectedFlavorIndex.value = index
                                addButtonScale.animateTo(0.8f,
                                    animationSpec = tween(100, easing = LinearEasing)
                                )
                                addButtonScale.animateTo(1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ProductNutritionSection(state = details.nutrition)
                    Spacer(modifier = Modifier.height(16.dp))

                    ProductDescriptionSection(productDescription = details.productDescription)

                    // Add more space at the bottom for better scrolling experience
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFF6B35),
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading product details...",
                style = AppTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ProductNotFoundScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Not Found",
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Product not found",
                style = AppTheme.typography.titleLarge,
                color = Color.Gray
            )
        }
    }
}

// ===== PRODUCT PREVIEW SECTION =====
@Composable
fun ProductPreviewSection(
    modifier: Modifier = Modifier,
    productType: String,
    productId: String,
    navController: NavController,
    productDetails: ProductDetails? = null,
    animatedScale: Float = 1f,
    headerBgAlpha: Float = 1f
) {
    Box(modifier = modifier.height(IntrinsicSize.Max)) {
        Column {
            ProductBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .graphicsLayer {
                        alpha = headerBgAlpha
                    }
            )

            productDetails?.let { preview ->
                PriceBox(
                    price = preview.price,
                    modifier = Modifier.height(50.dp)
                )
            }
        }

        productDetails?.let { preview ->
            Content(
                name = preview.name,
                imageUrl = preview.imageUrl,
                price = preview.price,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp),
                navController = navController,
                scale = animatedScale
            )
        }
    }
}

@Composable
private fun ProductBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.colors.secondarySurface,
            )
    )
}

@Composable
private fun PriceBox(
    modifier: Modifier = Modifier,
    price: Double
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFCD9B6))
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "$${String.format("%.2f", price)}",
            style = AppTheme.typography.titleLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    name: String,
    imageUrl: String,
    price: Double,
    navController: NavController,
    scale: Float = 1f
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (actionBar, productImg) = createRefs()
        ActionBar(
            headline = name,
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .constrainAs(actionBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end) },
            navController = navController
        )
        Image(
            painter = painterResource(id = getDrawableId(imageUrl)),
            contentDescription = name,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .height(256.dp)
                .scale(scale)
                .constrainAs(productImg) {
                    end.linkTo(parent.end)
                    top.linkTo(actionBar.bottom, margin = 20.dp)
                }
        )
    }
}

@Composable
private fun ActionBar(
    modifier: Modifier = Modifier,
    headline: String,
    navController: NavController
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = headline,
            style = AppTheme.typography.headline,
            color = AppTheme.colors.onSecondarySurface,
            fontSize = 24.sp
        )
        CloseButton(navController = navController)
    }
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Surface(
        modifier = modifier.size(44.dp)
            .clickable { navController.navigateUp() },
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.secondarySurface,
        contentColor = AppTheme.colors.secondarySurface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppTheme.colors.onSecondarySurface
            )
        }
    }
}

// ===== PRODUCT DESCRIPTION SECTION =====
@Composable
fun ProductDescriptionSection(
    modifier: Modifier = Modifier,
    productDescription: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Description",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Text(
            text = productDescription,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onBackground,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ===== PRODUCT NUTRITION SECTION =====
@Composable
fun ProductNutritionSection(
    modifier: Modifier = Modifier,
    state: ProductNutritionState
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NutritionSectionHeader(
            title = "Nutrition facts",
            calories = state.calories
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            state.nutrition.forEach { item ->
                NutritionItem(state = item)
            }
        }
    }
}

@Composable
private fun NutritionSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    calories: Calories
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = calories.value,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onBackground
            )
            Text(
                text = calories.unit,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onBackground
            )
        }
    }
}

@Composable
private fun NutritionItem(
    modifier: Modifier = Modifier,
    state: NutritionState
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.amount,
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = AppTheme.colors.onBackground
            )
            Text(
                text = state.unit,
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = AppTheme.colors.onBackground
            )
        }
        Text(
            text = state.title,
            style = AppTheme.typography.label,
            color = AppTheme.colors.onBackground
        )
    }
}

// ===== FLAVOR SECTION =====
@Composable
fun FlavorSection(
    modifier: Modifier = Modifier,
    data: List<ProductFlavorState>,
    selectedIndex: Int = -1,
    onFlavorSelected: (Int) -> Unit
) {
    Log.d("FlavorSection", "Rendering FlavorSection with ${data.size} flavors")
    data.forEach { flavor ->
        Log.d("FlavorSection", "Flavor: ${flavor.name}, Image: ${flavor.imgRes}, Price: ${flavor.price}")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Add More Flavor",
            emotion = "🍽️"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.forEachIndexed { index, item ->
                ProductFlavorItem(
                    state = item,
                    isSelected = index == selectedIndex,
                    onClick = { onFlavorSelected(index) },
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    emotion: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Text(
            text = emotion,
            style = AppTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun ProductFlavorItem(
    modifier: Modifier = Modifier,
    state: ProductFlavorState,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val drawableId = getDrawableId(state.imgRes)
    Log.d("FlavorSection", "Loading flavor image for ${state.name}: ${state.imgRes}, resolved to resource ID: $drawableId")

    val animatedScale = remember { Animatable(1f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            animatedScale.animateTo(1.05f,
                animationSpec = tween(200, easing = LinearEasing)
            )
        } else {
            animatedScale.animateTo(1f,
                animationSpec = tween(200, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .size(150.dp)
            .scale(animatedScale.value)
            .shadow(
                elevation = if (isSelected) 15.dp else 8.dp,
                spotColor = Color.LightGray,
                shape = RoundedCornerShape(28.dp)
            )
            .background(
                shape = RoundedCornerShape(28.dp),
                color = if (isSelected) Color(0xFFFFE6CC) else AppTheme.colors.regularSurface
            )

            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFFFFA500) else Color.Transparent,
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onClick() }
    ) {
        /*Column(
            modifier = Modifier.padding(
                vertical = 20.dp,
                horizontal = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )*/
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = state.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.name,
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFFFF6B35) else Color.DarkGray
                )
                Text(
                    text = "+$${"%.2f".format(state.price)}",
                    style = AppTheme.typography.bodySmall,
                    color = if (isSelected) Color(0xFFFFA500) else Color.Gray
                    //fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ===== ORDER ACTION BAR =====
@Composable
fun OrderActionBar(
    modifier: Modifier = Modifier,
    state: OrderState,
    onAddItemClicked: () -> Unit,
    onRemoveItemClicked: () -> Unit,
    onCheckOutClicked: () -> Unit,
    buttonScale: Float = 1f
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = AppTheme.colors.surface,
        contentColor = AppTheme.colors.onSurface,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(76.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Selector(
                amount = state.amount,
                onAddItemClicked = onAddItemClicked,
                onRemoveItemClicked = onRemoveItemClicked,
                modifier = Modifier.weight(weight = 1f),
                buttonScale = buttonScale
            )
            Cart(
                totalPrice = state.totalPrice,
                onClicked = onCheckOutClicked,
                modifier = Modifier.weight(weight = 1f),
                buttonScale = buttonScale
            )
        }
    }
}

@Composable
private fun Selector(
    modifier: Modifier = Modifier,
    amount: Int,
    onAddItemClicked: () -> Unit,
    onRemoveItemClicked: () -> Unit,
    buttonScale: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = AppTheme.colors.secondarySurface,
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectorButton(
                iconRes = R.drawable.ic_minus,
                containerColor = AppTheme.colors.actionSurface,
                contentColor = AppTheme.colors.onActionSurface,
                onClicked = onRemoveItemClicked,
                scale = buttonScale
            )
            Text(
                text = amount.toString(),
                color = AppTheme.colors.onSurface,
                style = AppTheme.typography.titleLarge
            )

            SelectorButton(
                iconRes = R.drawable.ic_plus,
                containerColor = AppTheme.colors.secondarySurface,
                contentColor = AppTheme.colors.onSecondarySurface,
                onClicked = onAddItemClicked,
                scale = buttonScale
            )
        }
    }
}

@Composable
private fun SelectorButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    containerColor: Color,
    contentColor: Color,
    onClicked: () -> Unit,
    scale: Float = 1f
) {
    Surface(
        modifier = modifier
            .size(24.dp)
            .scale(scale),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClicked),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(7.dp)
            )
        }
    }
}

@Composable
private fun Cart(
    modifier: Modifier = Modifier,
    totalPrice: String,
    onClicked: () -> Unit,
    buttonScale: Float = 1f
) {
    Surface(
        modifier = modifier
            .scale(buttonScale)
            .clickable(onClick = onClicked),
        color = AppTheme.colors.secondarySurface,
        contentColor = AppTheme.colors.onSecondarySurface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add to Cart",
                    style = AppTheme.typography.titleSmall
                )
                Text(
                    text = totalPrice,
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ===== ADDITIONAL UI COMPONENTS =====

@Composable
fun RatingSection(
    modifier: Modifier = Modifier,
    rating: Float = 4.5f,
    reviewCount: Int = 352
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Customer Reviews",
                style = AppTheme.typography.titleLarge,
                color = AppTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingStars(rating = rating)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$rating ($reviewCount reviews)",
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.onBackground
                )
            }
        }

        Button(
            onClick = { /* Navigate to reviews */ },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.secondarySurface
            )
        ) {
            Text("View All")
        }
    }
}

@Composable
fun RatingStars(
    rating: Float,
    maxStars: Int = 5,
    starSize: Float = 20f,
    starColor: Color = Color(0xFFFFA500)
) {
    Row {
        for (i in 1..maxStars) {
            val starFill = when {
                i <= rating.toInt() -> 1f
                i > rating.toInt() + 1 -> 0f
                else -> rating - rating.toInt()
            }

            StarIcon(
                fillRatio = starFill,
                size = starSize,
                color = starColor
            )
        }
    }
}

@Composable
fun StarIcon(
    fillRatio: Float,
    size: Float,
    color: Color
) {
    Box(
        modifier = Modifier.size(size.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_starborder),
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(size.dp)
        )

        if (fillRatio > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillRatio)
                    .height(size.dp)
                    .clip(ClipStartShape(fillRatio))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(size.dp)
                )
            }
        }
    }
}

// Custom shape for clipping star
private class ClipStartShape(private val fillRatio: Float) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): androidx.compose.ui.graphics.Outline {
        return androidx.compose.ui.graphics.Outline.Rectangle(
            androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = 0f,
                right = size.width * fillRatio,
                bottom = size.height
            )
        )
    }
}

@Composable
fun SimilarProductsSection(
    modifier: Modifier = Modifier,
    products: List<ProductPreviewState> = emptyList()
) {
    // If no products provided, return empty
    if (products.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "You May Also Like",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            products.forEach { product ->
                SimilarProductItem(product = product)
            }
        }
    }
}

@Composable
fun SimilarProductItem(
    modifier: Modifier = Modifier,
    product: ProductPreviewState
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = repeatable(
                iterations = 1,
                animation = keyframes {
                    durationMillis = 200
                    1f at 0
                    1.05f at 200
                },
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Column(
        modifier = modifier
            .width(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.regularSurface)
            .clickable { /* Navigate to product */ }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = getDrawableId(product.imageUrl)),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.onRegularSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$${product.price.format(2)}",
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onRegularSurface
        )
    }
}

// ===== ALLERGEN INFO SECTION =====
@Composable
fun AllergenInfoSection(
    modifier: Modifier = Modifier,
    allergens: List<String> = listOf("Nuts", "Milk", "Wheat", "Soy")
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Allergen Information",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "This product contains:",
                style = AppTheme.typography.body,
                color = AppTheme.colors.onBackground
            )
        }

        allergens.forEach { allergen ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.onBackground
                )

                Text(
                    text = allergen,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.onBackground
                )
            }
        }
    }
}

// ===== VIDEO TUTORIAL SECTION =====
@Composable
fun VideoTutorialSection(
    modifier: Modifier = Modifier,
    videoThumbnail: String = "thumbnail_preparation"
) {
    val isPlaying = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.LightGray
            )
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "How to Prepare",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .clickable { isPlaying.value = !isPlaying.value }
        ) {
            Image(
                painter = painterResource(id = getDrawableId(videoThumbnail)),
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (!isPlaying.value) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // Video is playing, show controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    )

                    // Video progress bar
                    LinearProgressIndicator(
                        progress = 0.4f, // Sample progress
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = Color(0xFFFF6B35)
                    )
                }
            }
        }

        Text(
            text = "Watch our chef's guide to preparing this dish at home with simple steps.",
            style = AppTheme.typography.body,
            color = AppTheme.colors.onBackground
        )
    }
}

// Helper function for TextOverflow
private object TextOverflow {
    val Ellipsis = androidx.compose.ui.text.style.TextOverflow.Ellipsis
}

// ===== DATA CLASSES =====
data class ProductDetails(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val productDescription: String,
    val nutrition: ProductNutritionState,
    val flavors: List<ProductFlavorState>
)

data class ProductPreviewState(
    val name: String,
    val imageUrl: String,
    val price: Double
)

data class ProductNutritionState(
    val calories: Calories,
    val nutrition: List<NutritionState>
)

data class Calories(
    val value: String,
    val unit: String
)

data class NutritionState(
    val amount: String,
    val unit: String,
    val title: String
)

data class ProductFlavorState(
    val name: String,
    val price: Double,
    val imgRes: String
)

data class OrderState(
    val amount: Int,
    val totalPrice: String
)

fun Double.format(digits: Int) = "%.${digits}f".format(this)

// ===== EXTENSIONS =====

// Extension function to animate visibility
@Composable
fun Modifier.animateVisibility(
    visible: Boolean,
    duration: Int = 300
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = duration),
        label = "visibility"
    )
    return this.graphicsLayer(alpha = alpha)
}

// Extension function to add pulse animation
@Composable
fun Modifier.pulseAnimation(
    enabled: Boolean = true,
    pulseMagnitude: Float = 0.03f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + pulseMagnitude,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    return if (enabled) {
        this.scale(scale)
    } else {
        this
    }
}

// Extension function to add shimmer effect
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    return this.background(brush)
}

// Complete Product Details Screen usage example:
@Composable
fun ProductDetailsScreenExample(navController: NavController) {
    ProductDetailsScreen(
        productType = "food",
        productId = "123",
        navController = navController
    )
 }
