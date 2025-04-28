package com.example.fooddeliveryapp.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.components.BottomNavBar
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var currentRoute by remember { mutableStateOf("home") }
    val scrollState = rememberScrollState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading delay
    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    BackHandler {
        (context as Activity).finish()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isLoading,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                currentRoute = route
                                if (route == "SearchBarSection") {
                                    navController.navigate("SearchBarSection")
                                } else {
                                    navController.navigate(route)
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            if (isLoading) {
                ShimmerEffect()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopSection(navController = navController)
                    Spacer(modifier = Modifier.height(8.dp))
                    TodaysMenuSection()
                    Spacer(modifier = Modifier.height(16.dp))
                    MenuSection(navController)
                    Spacer(modifier = Modifier.height(24.dp))
                    BestOfferSection(navController)
                }
            }
        }
    }
}

@Composable
fun ShimmerEffect() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile header shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Today's menu title shimmer
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Featured card shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Menu categories shimmer
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Best offer title shimmer
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Offer cards shimmer
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// TopSection Component with animation enhancements
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val userProfile by profileViewModel.userProfile.collectAsState()

    val headerElevation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerElevation.animateTo(
            targetValue = 8f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .zIndex(1f)
    ) {
        UserProfileHeader(
            userName = userProfile?.name ?: "",
            navController = navController,
            elevation = headerElevation.value
        )
    }
}

@Composable
fun UserProfileHeader(userName: String, navController: NavController, elevation: Float) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.95f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .scale(scale.value)
            .shadow(elevation = elevation.dp, shape = RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                navController.navigate("profileView")
            },
        color = Color(0xFFFFCC80),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_profile),
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (userName.isNotEmpty()) "Welcome back, $userName!"
                    else "Welcome!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "How hungry are you?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// TodaysMenuSection Component with enhanced animations
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysMenuSection() {
    val cards = listOf(
        CardData("Free Donut!",
            "For orders over $20",
            R.drawable.img_donut,
            Color(0xFF79be95)
        ),
        CardData("Free Drinks!",
            "With every Burger of $10",
            R.drawable.img_drinks,
            Color(0xFF704747)
        ),
        CardData("Free Cakes!",
            "For orders over $10",
            R.drawable.img_cake,
            Color(0xFFC4A44E)
        ),
        CardData(
            "Free Shawarma",
            "For 4 Shawarma purchase",
            R.drawable.img_shawarma,
            Color(0xFF886532)
        )
    )
    var currentCardIndex by remember { mutableStateOf(0) }
    val offsetAnimation1 = remember { Animatable(0f) }
    val offsetAnimation2 = remember { Animatable(400f) }
    val cardScale = remember { Animatable(1f) }
    val cardRotation = remember { Animatable(0f) }
    val imageScale = remember { Animatable(1f) }

    // Dot indicators
    val dotCount = cards.size
    val dotSelectedColor = Color(0xFF000000)
    val dotUnselectedColor = Color(0xFFCCCCCC)

    LaunchedEffect(currentCardIndex) {
        while (true) {
            delay(3000)
            coroutineScope {
                // Launch all animations simultaneously
                launch {
                    offsetAnimation1.animateTo(
                        targetValue = -400f,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    offsetAnimation2.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    // Add subtle scale animation
                    cardScale.animateTo(
                        targetValue = 0.92f,
                        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
                    )
                    cardScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 500, easing = EaseInCubic)
                    )
                }
                launch {
                    // Add subtle rotation animation
                    cardRotation.animateTo(
                        targetValue = 2f,
                        animationSpec = tween(durationMillis = 400, easing = EaseOutCubic)
                    )
                    cardRotation.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 600, easing = EaseInCubic)
                    )
                }
                launch {
                    // Image pop animation
                    imageScale.animateTo(
                        targetValue = 1.15f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                    imageScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                }
            }

            currentCardIndex = (currentCardIndex + 1) % cards.size
            offsetAnimation1.snapTo(0f)
            offsetAnimation2.snapTo(400f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Menu",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Animated dots as card indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                repeat(dotCount) { index ->
                    val isSelected = index == currentCardIndex
                    val size by animateFloatAsState(
                        targetValue = if (isSelected) 10f else 8f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f)
                    )
                    Box(
                        modifier = Modifier
                            .size(size.dp)
                            .background(
                                color = if (isSelected) dotSelectedColor else dotUnselectedColor,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val cardScaleValue = if (offsetAnimation1.value < -200f) cardScale.value else 1f
            val cardRotationValue = if (offsetAnimation1.value < -200f) cardRotation.value else 0f

            FeaturedCard(
                title = cards[currentCardIndex].title,
                subtitle = cards[currentCardIndex].subtitle,
                imageResId = cards[currentCardIndex].imageResId,
                backgroundColor = cards[currentCardIndex].backgroundColor,
                offset = offsetAnimation1.value,
                scale = cardScaleValue,
                rotation = cardRotationValue,
                imageScale = imageScale.value
            )

            Spacer(modifier = Modifier.width(16.dp))

            val nextCardScaleValue = if (offsetAnimation2.value < 200f) cardScale.value else 1f
            val nextCardRotationValue = if (offsetAnimation2.value < 200f) cardRotation.value else 0f

            FeaturedCard(
                title = cards[(currentCardIndex + 1) % cards.size].title,
                subtitle = cards[(currentCardIndex + 1) % cards.size].subtitle,
                imageResId = cards[(currentCardIndex + 1) % cards.size].imageResId,
                backgroundColor = cards[(currentCardIndex + 1) % cards.size].backgroundColor,
                offset = offsetAnimation2.value,
                scale = nextCardScaleValue,
                rotation = nextCardRotationValue,
                imageScale = imageScale.value
            )
        }
    }
}

data class CardData(
    val title: String,
    val subtitle: String,
    val imageResId: Int,
    val backgroundColor: Color
)

@Composable
fun FeaturedCard(
    title: String,
    subtitle: String,
    imageResId: Int,
    backgroundColor: Color,
    offset: Float,
    scale: Float = 1f,
    rotation: Float = 0f,
    imageScale: Float = 1f
) {
    Box(
        modifier = Modifier
            .width(380.dp)
            .height(140.dp)
            .offset(x = offset.dp)
            .scale(scale)
            .rotate(rotation)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Add subtle gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = 0.8f),
                                    backgroundColor.copy(alpha = 0.5f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Image with pop animation
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (0).dp)
                .offset(y = (-30).dp)
                .scale(imageScale)
                /*.graphicsLayer {
                    alpha = 0.95f
                    shadowElevation = 16f
                }
                 */
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

// MenuSection Component with staggered grid layout
@Composable
fun MenuSection(navController: NavController) {
    val menuCategories = listOf(
        MenuCategory(
            "Burgers",
            R.drawable.img_burger1,
            Color(0xFFFFD700)
        ),
        MenuCategory(
            "Fries",
            R.drawable.image_fries,
            Color(0xFFFFA500)
        ),
        MenuCategory(
            "Drinks",
            R.drawable.img_drinks,
            Color(0xFF77AADD)
        ),
        MenuCategory(
            "Pasta",
            R.drawable.img_pasta,
            Color(0xFF8BC34A)
        ),
        MenuCategory(
            "Juices",
            R.drawable.img_juice,
            Color(0xFFFF6347)
        ),
        MenuCategory(
            "Cheesecakes",
            R.drawable.img_cheesecake,
            Color(0xFFF06292)
        ),
        MenuCategory(
            "Shawarmas",
            R.drawable.image_shawarma,
            Color(0xFF816152)
        ),
        MenuCategory(
            "Donuts",
            R.drawable.image_donut,
            Color(0xFFFFBE00)
        )

    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Categories",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        StaggeredGrid(menuCategories, navController)
    }
}

@Composable
fun StaggeredGrid(categories: List<MenuCategory>, navController: NavController) {
    val itemSizes = listOf(
        Pair(2, 1),  // 2/3 width, normal height
        Pair(1, 1),  // 1/3 width, normal height
        Pair(1, 1),  // 1/3 width, normal height
        Pair(1, 1),  // 1/3 width, normal height
        Pair(2, 1)   // 2/3 width, normal height
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                category = categories[0],
                modifier = Modifier
                    .weight(2f)
                    .height(130.dp),
                onClick = { navController.navigate("BurgerScreen") }
            )
            MenuCard(
                category = categories[1],
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                onClick = { navController.navigate("FryScreen") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                category = categories[2],
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                onClick = { navController.navigate("DrinkScreen") }
            )
            MenuCard(
                category = categories[3],
                modifier = Modifier
                    .weight(2f)
                    .height(130.dp),
                onClick = { navController.navigate("PastaScreen") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                category = categories[4],
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                onClick = { navController.navigate("JuiceScreen") }
            )
            MenuCard(
                category = categories[5],
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                onClick = { navController.navigate("CheesecakeScreen") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                category = categories[6],
                modifier = Modifier
                    .weight(2.2f)
                    .height(130.dp),
                onClick = { navController.navigate("ShawarmaScreen") }
            )
            MenuCard(
                category = categories[7],
                modifier = Modifier
                    .weight(1.2f)
                    .height(130.dp),
                onClick = { navController.navigate("DonutScreen") }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(
    category: MenuCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 300f
        )
    )

    val elevation = animateFloatAsState(
        targetValue = if (isPressed.value) 4f else 8f,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale.value)
            .shadow(elevation = elevation.value.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = category.backgroundColor),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient overlay for better text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                category.backgroundColor.copy(alpha = 0.7f),
                                category.backgroundColor
                            )
                        )
                    )
            )

            // Title with better positioning
            Text(
                text = category.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

            // Large image with improved positioning and shadow
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (15).dp, y = (15).dp)
                    /*.graphicsLayer {
                        alpha = 0.9f
                        shadowElevation = 8f
                    }*/
            )
        }
    }
}

data class MenuCategory(
    val label: String,
    val imageResId: Int,
    val backgroundColor: Color
)

// BestOfferSection Component with improved cards
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestOfferSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Best Offers",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add an animated reveal for offer cards
        val offers = remember { sampleOffers }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            offers.forEachIndexed { index, offer ->
                // Staggered animation delay based on index
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 150L)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInHorizontally(initialOffsetX = { it * (index % 2 * 2 - 1) }) +
                            fadeIn() + expandVertically()
                ) {
                    OfferCard(
                        offer = offer,
                        onOfferClick = {
                            navController.navigate("productDetailsScreen")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfferCard(
    offer: FoodOffer,
    onOfferClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 300f
        )
    )

    val elevation = animateFloatAsState(
        targetValue = if (isPressed.value) 2f else 6f,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .scale(scale.value)
            .shadow(elevation = elevation.value.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEFEC4)),
        shape = RoundedCornerShape(16.dp),
        onClick = onOfferClick,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = offer.imageRes),
                    contentDescription = offer.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                        //.shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = offer.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = offer.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Add price and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offer.price,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Star rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Rating",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = offer.rating,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private data class FoodOffer(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val price: String = "$8.99",
    val rating: String = "4.8"
)

private val sampleOffers = listOf(
    FoodOffer(R.drawable.img_burger1, "Deshi Chicken Burger", "Tasty and Juicy", "$9.99", "4.9"),
    FoodOffer(R.drawable.img_shawarma, "Turkish Shawarma", "Delicious and Cravy", "$7.99", "4.7"),
    FoodOffer(R.drawable.img_fries, "French Fries", "Fresh and Crispy", "$4.99", "4.5")
)