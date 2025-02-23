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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Drink(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val productDescription: String = ""  // Added to match Burger structure
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinksScreen(navController: NavController) {
    val drinks = remember { mutableStateListOf<Drink>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("drinks")
            .get()
            .addOnSuccessListener { result ->
                val fetchedDrinks = result.documents.mapNotNull { doc ->
                    try {
                        Drink(
                            id = when (val idValue = doc.get("id")) {
                                is Number -> idValue.toInt()
                                is String -> idValue.toIntOrNull() ?: 0
                                else -> 0
                            },
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            productDescription = doc.getString("productDescription") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing document: ${doc.id}", e)
                        null
                    }
                }
                drinks.clear()
                drinks.addAll(fetchedDrinks)
                loading = false
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching drinks", e)
                loading = false
            }
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
                DrinkCard(drink = drink, navController = navController)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkCard(drink: Drink, navController: NavController) {
    val imageResId = getDrinkImageResourceId(drink.imageUrl)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                Log.d("Navigation", "Navigating to drink with ID: ${drink.id}")
                navController.navigate("productDetailsScreen/${drink.id}")
            },
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
                painter = painterResource(id = imageResId),
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
        }
    }
}

fun getDrinkImageResourceId(imageName: String): Int {
    return when (imageName) {
        "img_kinzacola" -> R.drawable.img_kinzacola
        "img_kinzalemon" -> R.drawable.img_kinzalemon
        "img_kinzaorange" -> R.drawable.img_kinzaorange
        "img_kinzablackcurrant" -> R.drawable.img_kinzablackcurrant
        "img_pamircola1" -> R.drawable.img_pamircola1
        "img_pamirlemonlime" -> R.drawable.img_pamirlemonlime
        else -> R.drawable.img_placeholder
    }
}