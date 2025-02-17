package com.example.fooddeliveryapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.components.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProductDetailsScreen(burgerId: String) {
    val db = FirebaseFirestore.getInstance()
    var productDetails by remember { mutableStateOf<ProductDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    Log.d("ProductDetailsScreen", "Received burgerId: $burgerId")

    LaunchedEffect(Unit) {
        try {
            val docRef = db.collection("productdetails").document("burger$burgerId")
            Log.d("Firestore", "Attempting to Fetching document: burger$burgerId")
            val document = docRef.get().await()
            Log.d("Firestore", "Document exists: ${document.exists()}")
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    Log.d("Firestore", "Document data: $data")
                    val nutritionData = data["nutrition"] as? Map<String, Any> ?: emptyMap()
                    val flavorsData = data["flavors"] as? List<Map<String, Any>> ?: emptyList()

                    productDetails = ProductDetails(
                        id = (data["id"] as? Long) ?: 0,
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
                                price = it["price"] as? String ?: "",
                                imgRes = it["imgRes"] as? String ?: ""
                            )
                        }
                    )
                }
            } else {
                Log.e("Firestore", "Document does not exist for burgerId: burger$burgerId")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching product details", e)
        } finally {
            isLoading = false
        }
    }

    // UI layout
    Column(modifier = Modifier.padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
        } else {
            productDetails?.let { details ->
                ProductPreviewSection(burgerId = details.id.toInt())

                Spacer(modifier = Modifier.height(16.dp))

                // Passing dynamic data to the flavor section
                FlavorSection(data = details.flavors)
                Spacer(modifier = Modifier.height(16.dp))

                // Passing dynamic data to the nutrition section
                ProductNutritionSection(state = details.nutrition)
                Spacer(modifier = Modifier.height(16.dp))

                // Passing dynamic description
                ProductDescriptionSection(productDescription = details.productDescription)
            } ?: Text("Product not found", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
        }
    }
}

// 🔹 Function to get drawable resource ID from a string name
fun getDrawableId(imgRes: String): Int {
    return when (imgRes) {
        "img_cheese" -> R.drawable.img_cheese
        "img_bacon" -> R.drawable.img_bacon
        "img_onion" -> R.drawable.img_onion
        else -> R.drawable.img_placeholder // Default fallback image
    }
}

// Data class for product details
data class ProductDetails(
    val id: Long,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val productDescription: String,
    val nutrition: ProductNutritionState,
    val flavors: List<ProductFlavorState>
)