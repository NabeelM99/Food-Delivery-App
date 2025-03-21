package com.example.fooddeliveryapp.ui.screen.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun FlavorSection(
    modifier: Modifier = Modifier,
    data: List<ProductFlavorState>
) {
    Log.d("FlavorSection", "Rendering FlavorSection with ${data.size} flavors")
    data.forEach { flavor ->
        Log.d("FlavorSection", "Flavor: ${flavor.name}, Image: ${flavor.imgRes}, Price: ${flavor.price}")
    }

    Column(
        modifier = modifier
    ) {
        SectionHeader(
            title = "Add More Flavor",
            emotion = ""
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.forEach { item ->
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
    val drawableId = getDrawableId(state.imgRes)
    Log.d("FlavorSection", "Loading flavor image for ${state.name}: ${state.imgRes}, resolved to resource ID: $drawableId")

    Box(
        modifier = modifier
            .size(150.dp)
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
                painter = painterResource(id = drawableId),
                contentDescription = state.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.name.ifEmpty { "Unknown Flavor" },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onRegularSurface
                )
                Text(
                    text = "+$${"%.2f".format(state.price)}", // Format price as currency
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onRegularSurface
                )
            }
        }
    }
}

// Data class for flavor state with updated price type.
data class ProductFlavorState(
    val name: String,
    val price: Double,
    val imgRes: String
)
