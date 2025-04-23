package com.example.fooddeliveryapp.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    BackHandler {
        (context as Activity).finish()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
                            if (route == "SearchBarSection") {
                                navController.navigate("SearchBarSection") // Navigate to SearchScreen
                            } else {
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopSection(navController = navController)
                Spacer(modifier = Modifier.height(2.dp))
                TodaysMenuSection()
                Spacer(modifier = Modifier.height(2.dp))
                MenuSection(navController)
                Spacer(modifier = Modifier.height(20.dp))
                BestOfferSection(navController)
            }
        }
    }
}

// TopSection Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val userProfile by profileViewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .zIndex(1f)
    ) {
        UserProfileHeader(
            userName = userProfile?.name ?: "",
            navController = navController
        )
    }
}

@Composable
fun UserProfileHeader(userName: String, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { navController.navigate("profileView") },
        color = Color(0xFFFFCC80),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.user_profile),
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

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

// TodaysMenuSection Component
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
    var currentCardIndex by remember {
        mutableStateOf(0)
    }
    val offsetAnimation1 = remember {
        Animatable(0f)
    }
    val offsetAnimation2 = remember {
        Animatable(400f)
    }

    LaunchedEffect(currentCardIndex) {
        while (true) {
            delay(3000)
            coroutineScope {
                // Launch both animations simultaneously using coroutineScope
                launch {
                    offsetAnimation1.animateTo(
                        targetValue = -400f,
                        animationSpec = tween(durationMillis = 1000)
                    )
                }
                launch {
                    offsetAnimation2.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 1000)
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
        Text(
            text = "Today's Menu",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            FeaturedCard(
                title = cards[currentCardIndex].title,
                subtitle = cards[currentCardIndex].subtitle,
                imageResId = cards[currentCardIndex].imageResId,
                backgroundColor = cards[currentCardIndex].backgroundColor,
                offset = offsetAnimation1.value
            )

            Spacer(modifier = Modifier.width(16.dp)) // Adding space between cards

            FeaturedCard(
                title = cards[(currentCardIndex + 1) % cards.size].title,
                subtitle = cards[(currentCardIndex + 1) % cards.size].subtitle,
                imageResId = cards[(currentCardIndex + 1) % cards.size].imageResId,
                backgroundColor = cards[(currentCardIndex + 1) % cards.size].backgroundColor,
                offset = offsetAnimation2.value
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
    offset: Float
) {
    Box(
        modifier = Modifier
            .width(380.dp)
            .height(130.dp)
            .offset(x = offset.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .width(300.dp)
                .height(150.dp),
            elevation = CardDefaults
                .cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (0).dp)
                .offset(y = (-30).dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

// MenuSection Component
@OptIn(ExperimentalMaterial3Api::class)
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
            R.drawable.img_fries,
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
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            menuCategories.forEach { category ->
                MenuCard(
                    category = category,
                    onClick = {
                        when (category.label) {
                            "Burgers" -> navController.navigate("BurgerScreen")
                            "Fries" -> navController.navigate("FryScreen")
                            "Drinks" -> navController.navigate("DrinkScreen")
                            "Juices" -> navController.navigate("JuiceScreen")
                            "Pasta" -> navController.navigate("PastaScreen")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(
    category: MenuCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = category.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title at the top
            Text(
                text = category.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            // Large image at bottom-left
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .wrapContentSize(align = Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 40.dp)
            )
        }
    }
}

data class MenuCategory(
    val label: String,
    val imageResId: Int,
    val backgroundColor: Color
)

// BestOfferSection Component
@Composable
fun BestOfferSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Best Offer",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sampleOffers.forEach { offer ->
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

@Composable
private fun OfferCard(
    offer: FoodOffer,
    onOfferClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEFEC4)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onOfferClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = offer.imageRes),
                contentDescription = offer.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = offer.title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = offer.subtitle,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private data class FoodOffer(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
)

private val sampleOffers = listOf(
    FoodOffer(R.drawable.img_burger1, "Deshi Chicken Burger", "Tasty and Juicy"),
    FoodOffer(R.drawable.img_shawarma, "Turkish Shawarma", "Delicious and Cravy"),
    FoodOffer(R.drawable.img_fries, "French Fries", "Fresh and Crispy")
)