package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import android.annotation.SuppressLint
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
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R

data class Drink(
    val id: Int,
    val name: String,
    val price: Double,
    val imageResId: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinksScreen(navController: NavController) {
    val drinks = remember {
        listOf(
            Drink(
                1,
                "Kinza Cola",
                5.99,
                R.drawable.img_kinzacola,
                "250ml Kinza Cola Carbonated Soft Drink"
            ),
            Drink(
                2,
                "Kinza Lemon",
                7.99,
                R.drawable.img_kinzalemon,
                "250ml Kinza Lemon Carbonated Soft Drink"
            ),
            Drink(
                3,
                "Kinza Orange",
                6.49,
                R.drawable.img_kinzaorange,
                "250ml Kinza Orange Carbonated Soft Drink"
            ),
            Drink(
                4,
                "Kinza Black Currant",
                5.49,
                R.drawable.img_kinzablackcurrant,
                "330ml Kinza Blackcurrant Carbonated Soft Drink"
            ),
            Drink(
                5,
                "Shafa Pomegranate",
                5.94,
                R.drawable.img_pamircola1,
                "330ml Shafa Pomegranate Carbonated Soft Drink"
            ),
            Drink(
                6,
                "Pamir Lemon Lime",
                6.94,
                R.drawable.img_pamirlemonlime,
                "330ml Pamir Cola Lemon Lime Carbonated Soft Drink"
            )



        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drinks") },
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
            items(drinks) { drink ->
                DrinkCard(drink = drink)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkCard(drink: Drink) {
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

            Image(
                painter = painterResource(id = drink.imageResId),
                contentDescription = drink.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = drink.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = drink.description,
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
                        text = "$${String.format("%.2f", drink.price)}",
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
