package com.example.fooddeliveryapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.components.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProductDetailsScreen(
    productType: String,
    productId: String,
    navController: NavController,
    cardViewModel: CartViewModel = viewModel()
) {
    val db = FirebaseFirestore.getInstance()
    var productDetails by remember { mutableStateOf<ProductDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //Log.d("ProductDetailsScreen", "Received burgerId: $burgerId")

    LaunchedEffect(productType, productId) {
        try {
            val docRef = if (productType == "burger") {
                db.collection("productdetails").document("burger$productId")
            } else {
                db.collection("productdetails").document(productId)
            }
            //val docRef = db.collection("productdetails").document("burger$burgerId")
            //Log.d("Firestore", "Attempting to Fetching document: burger$burgerId")
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
                Log.e("Firestore", "Document does not exist for productId: $productId")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching product details", e)
        } finally {
            isLoading = false
        }
    }

    //added
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
        return
    }
    // Show error state if product details are not found
    if (productDetails == null) {
        Text("Product not found", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
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
                    onAddItemClicked = { amount++ },
                    onRemoveItemClicked = { if (amount > 1) amount-- },
                    onCheckOutClicked = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Item is added to the cart")
                            val item = CartItem(
                                id = productDetails?.id ?: 0,
                                name = productDetails?.name ?: "",
                                price = productDetails?.price ?: 0.0,
                                imageName = productDetails?.imageUrl ?: "",
                                quantity = amount
                            )
                            cardViewModel.addToCart(item)
                        }
                    }

                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        } else {
            productDetails?.let { details ->
                // Make content scrollable with LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues) // Apply scaffold padding
                        .padding(horizontal = 16.dp), // Horizontal padding
                    contentPadding = PaddingValues(bottom = 16.dp) // Add padding at the bottom
                ) {
                    item {
                        ProductPreviewSection(
                            burgerId = details.id.toInt(),
                            navController = navController
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlavorSection(data = details.flavors)

                        Spacer(modifier = Modifier.height(16.dp))

                        ProductNutritionSection(state = details.nutrition)

                        Spacer(modifier = Modifier.height(16.dp))

                        ProductDescriptionSection(productDescription = details.productDescription)
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Product not found")
            }
        }
    }
}

// Helper function to format double values
fun Double.format(digits: Int) = "%.${digits}f".format(this)

// 🔹 Function to get drawable resource ID from a string name
fun getDrawableId(imageName: String): Int {
    return when (imageName) {
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
