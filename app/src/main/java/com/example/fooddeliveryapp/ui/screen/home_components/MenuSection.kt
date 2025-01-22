package com.example.fooddeliveryapp.ui.screen.home_components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddeliveryapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSection(onCategoryClick: (MenuCategory) -> Unit = {}) {
    val menuCategories = listOf(
        MenuCategory("Burgers",
            R.drawable.img_burger1,
            Color(0xFFFFD700)
        ),
        MenuCategory("Fries",
            R.drawable.img_fries,
            Color(0xFFFFA500)
        ),
        MenuCategory("Drinks",
            R.drawable.img_drinks,
            Color(0xFF77AADD)
        ),
        MenuCategory("Pasta",
            R.drawable.img_pasta,
            Color(0xFF8BC34A)
        ),
        MenuCategory("Juice",
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
                    onClick = { onCategoryClick(category) }
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