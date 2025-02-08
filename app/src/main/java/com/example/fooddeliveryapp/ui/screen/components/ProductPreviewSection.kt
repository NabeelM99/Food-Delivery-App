package com.example.fooddeliveryapp.ui.screen.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProductPreviewSection(
    modifier: Modifier = Modifier,
    burgerId: Int
) {
    // State to hold the fetched product preview data
    var productPreviewState by remember { mutableStateOf<ProductPreviewState?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Fetch product preview data from Firebase
    LaunchedEffect(burgerId) {
        try {
            val db = FirebaseFirestore.getInstance()
            val document = db.collection("productdetails")
                .document("burger$burgerId")
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    productPreviewState = ProductPreviewState(
                        name = data["name"] as String,
                        imageUrl = data["imageUrl"] as String,
                        price = data["price"] as Double
                    )
                }
            } else {
                Log.e("Firestore", "Document does not exist for burgerId: $burgerId")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching product preview data", e)
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
        ProductBackground(
            modifier = Modifier.padding(bottom = 24.dp)
        )
        productPreviewState?.let { preview ->
            Content(
                name = preview.name,
                imageUrl = preview.imageUrl,
                price = preview.price,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp)
            )
        }
    }
}

@Composable
private fun ProductBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = AppTheme.colors.secondarySurface,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    )
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    name: String,
    imageUrl: String,
    price: Double
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (actionBar, productImg, highlightsSection) = createRefs()
        ActionBar(
            headline = name,
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .constrainAs(actionBar) { top.linkTo(parent.top) }
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
private fun ActionBar(modifier: Modifier = Modifier, headline: String) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = headline,
            style = AppTheme.typography.headline,
            color = AppTheme.colors.onSecondarySurface
        )
        CloseButton()
    }
}

@Composable
private fun CloseButton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.secondarySurface,
        contentColor = AppTheme.colors.secondarySurface
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        "img_classiccheeseburger" -> R.drawable.img_classiccheeseburger
        "img_doubleburger" -> R.drawable.img_doubleburger
        "img_chickenburger" -> R.drawable.img_chickenburger
        "img_veggieburger" -> R.drawable.img_veggieburger
        "img_beefburger" -> R.drawable.img_beefburger
        else -> R.drawable.img_placeholder
    }
}

// Data classes
data class ProductPreviewState(
    val name: String,
    val imageUrl: String,
    val price: Double
)