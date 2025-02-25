package com.example.fooddeliveryapp.ui.screen.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProductPreviewSection(
    modifier: Modifier = Modifier,
    productType: String,
    productId: String,
    navController: NavController
) {
    // State to hold the fetched product preview data
    var productPreviewState by remember { mutableStateOf<ProductPreviewState?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Fetch product preview data from Firebase
    LaunchedEffect(productId) {
        try {
            val db = FirebaseFirestore.getInstance()
            val documentId = if (productType == "burger") "burger$productId" else productId
            Log.d("Firestore", "Attempting to fetch document: $documentId")
            val document = db.collection("productdetails")
                .document(documentId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    Log.d("Firestore", "Fetched data: $data")
                    val price = data["price"] as? Double ?: 0.0
                    Log.d("Firestore", "Price value from Firebase: $price")

                    productPreviewState = ProductPreviewState(
                        name = data["name"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        price = price
                    )

                    Log.d("Firestore", "Successfully mapped data: $productPreviewState")
                }
            } else {
                //Log.e("Firestore", "Document does not exist for burgerId: $burgerId")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching product preview data", e)
            Log.e("Firestore", "Stack trace: ${e.stackTrace.joinToString("\n")}")
        } finally {
            loading = false
        }
    }

    // Show loading state while fetching data
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error state if product preview data is not found
    if (productPreviewState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Product preview not found", color = AppTheme.colors.onBackground)
        }
        return
    }

    // Display the product preview section
    Box(modifier = modifier.height(IntrinsicSize.Max)) {
        Column {
            ProductBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)  // Reduced height
            )

            // New light orange box for price
            productPreviewState?.let { preview ->
                Log.d("ProductPreview", "Price to display: ${preview.price}")
                PriceBox(
                    price = preview.price,
                    modifier = Modifier.height(50.dp)
                )
            }
        }
        productPreviewState?.let { preview ->
            Content(
                name = preview.name,
                imageUrl = preview.imageUrl,
                price = preview.price,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp),
                navController = navController
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
                //shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
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
            .background(Color(0xFFFCD9B6))  // Light orange color
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
    navController: NavController
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
                .constrainAs(productImg) {
                    end.linkTo(parent.end)
                    top.linkTo(actionBar.bottom, margin = 20.dp)
                }
        )
    }
}

@Composable
private fun ActionBar(modifier: Modifier = Modifier,
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
        Box(modifier = Modifier.fillMaxSize(),
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

// Helper function to get drawable resource ID from a string
fun getDrawableId(imageName: String): Int {
    return when (imageName) {
        //burger
        "img_classiccheeseburger" -> R.drawable.img_classiccheeseburger
        "img_doubleburger" -> R.drawable.img_doubleburger
        "img_chickenburger" -> R.drawable.img_chickenburger
        "img_veggieburger" -> R.drawable.img_veggieburger
        "img_beefburger" -> R.drawable.img_beefburger
        //juice
        "img_orangejuice" -> R.drawable.img_orangejuice
        "img_papayajuice" -> R.drawable.img_papayajuice
        "img_watermelonjuice" -> R.drawable.img_watermelonjuice
        "img_pineapplejuice" -> R.drawable.img_pineapplejuice
        "img_avocadojuice" -> R.drawable.img_avocadojuice
        "img_mangojuice" -> R.drawable.img_mangojuice
        //drinks
        "img_kinzablackcurrant" -> R.drawable.img_kinzablackcurrant
        "img_kinzacitrus" -> R.drawable.img_kinzacitrus
        "img_kinzalemon" -> R.drawable.img_kinzalemon
        "img_kinzaorange" -> R.drawable.img_kinzaorange
        "img_pamircola1" -> R.drawable.img_pamircola1
        "img_pamirlemonlime" -> R.drawable.img_pamirlemonlime

        else -> R.drawable.img_placeholder
    }
}

// Data classes
data class ProductPreviewState(
    val name: String,
    val imageUrl: String,
    val price: Double
)