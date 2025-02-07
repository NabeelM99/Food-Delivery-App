package com.example.fooddeliveryapp.ui.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun FlavorSection(
    modifier: Modifier = Modifier,
    data: List<ProductFlavorState> = ProductFlavorData // Default data included here
) {
    Column(
        modifier = modifier
    ) {
        SectionHeader(
            title = "Add More Flavor",
            emotion = "\uD83E\uDD29"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.onEach { item ->
                ProductFlavorItem(
                    state = item,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    emotion: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Text(
            text = emotion,
            style = AppTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun ProductFlavorItem(
    modifier: Modifier = Modifier,
    state: ProductFlavorState
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                spotColor = Color.LightGray,
                shape = RoundedCornerShape(28.dp)
            )
            .background(
                shape = RoundedCornerShape(28.dp),
                color = AppTheme.colors.regularSurface
            )
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = 20.dp,
                horizontal = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = state.imgRes),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.name,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onRegularSurface
                )
                Text(
                    text = "+${state.price}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onRegularSurface
                )
            }
        }
    }
}

// Data class and default data moved here from ProductFlavorData.kt
data class ProductFlavorState(
    val name: String,
    val price: String,
    val imgRes: Int
)

val ProductFlavorData = listOf(
    ProductFlavorState(
        imgRes = R.drawable.img_cheese,
        name = "Cheddar",
        price = "$0.79"
    ),
    ProductFlavorState(
        imgRes = R.drawable.img_bacon,
        name = "Halal Bacon",
        price = "$0.52"
    ),
    ProductFlavorState(
        imgRes = R.drawable.img_onion,
        name = "Onion",
        price = "$0.28"
    )
)