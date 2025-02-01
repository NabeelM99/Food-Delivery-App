package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import androidx.compose.foundation.Image
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
import com.example.fooddeliveryapp.R

data class Burger(
    val id: Int,
    val name: String,
    val price: Double,
    val imageResId: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurgerScreen(onBackClick: () -> Unit = {}) {
    val burgers = remember {
        listOf(
            Burger(
                1,
                "Classic Cheeseburger",
                5.99,
                R.drawable.img_burger1,
                "Juicy beef patty with melted cheese"
            ),
            Burger(
                2,
                "Double Beef Burger",
                7.99,
                R.drawable.img_burger1,
                "Double the beef, double the flavor"
            ),
            Burger(
                3,
                "Chicken Burger",
                6.49,
                R.drawable.img_burger1,
                "Crispy chicken with fresh lettuce"
            ),
            Burger(
                4,
                "Veggie Burger",
                5.49,
                R.drawable.img_burger1,
                "Plant-based patty with fresh veggies"
            ),
            // Add more burgers as needed
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Burgers") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                BurgerCard(burger = burger)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurgerCard(burger: Burger) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Burger Image
            Image(
                painter = painterResource(id = burger.imageResId),
                contentDescription = burger.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Burger Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = burger.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = burger.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Price and Add to Cart Button
            Button(
                onClick = { /* Handle add to cart */ },
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$${String.format("%.2f", burger.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
