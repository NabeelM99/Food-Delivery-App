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

data class Juice(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val productDescription: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuiceScreen(navController: NavController) {
    val juices = remember { mutableStateListOf<Juice>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("juices")
            .get()
            .addOnSuccessListener { result ->
                val fetchedJuices = result.documents.mapNotNull { doc ->
                    try {
                        Juice(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            productDescription = doc.getString("productDescription") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing juice: ${doc.id}", e)
                        null
                    }
                }
                juices.clear()
                juices.addAll(fetchedJuices)
                loading = false
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching juices", e)
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juices") },
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
            items(juices) { juice ->
                JuiceCard(juice = juice, navController = navController)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuiceCard(juice: Juice, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                Log.d("Navigation", "Navigating to juice with ID: ${juice.id}")
                navController.navigate("productDetailsScreen/juices/${juice.id}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getJuiceImageResourceId(juice.imageUrl)),
                contentDescription = juice.name,
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
                    text = juice.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = juice.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getJuiceImageResourceId(imageName: String): Int {
    return when (imageName) {
        "img_orangejuice" -> R.drawable.img_orangejuice
        "img_mangojuice" -> R.drawable.img_mangojuice
        "img_avocadojuice" -> R.drawable.img_avocadojuice
        "img_pineapplejuice" -> R.drawable.img_pineapplejuice
        "img_papayajuice" -> R.drawable.img_papayajuice
        "img_watermelonjuice" -> R.drawable.img_watermelonjuice
        else -> R.drawable.img_placeholder
    }
}