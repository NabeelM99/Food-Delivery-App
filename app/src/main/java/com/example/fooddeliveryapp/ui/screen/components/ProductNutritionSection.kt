package com.example.fooddeliveryapp.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun ProductNutritionSection(
    modifier: Modifier = Modifier,
    state: ProductNutritionState = ProductNutritionData // Default data included here
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Nutrition facts",
            calories = state.calories
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            state.nutrition.onEach { item ->
                NutritionItem(state = item)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    calories: Calories
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = calories.value,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onBackground
            )
            Text(
                text = calories.unit,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onBackground
            )
        }
    }
}

@Composable
private fun NutritionItem(
    modifier: Modifier = Modifier,
    state: NutritionState
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.amount,
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = AppTheme.colors.onBackground
            )
            Text(
                text = state.unit,
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = AppTheme.colors.onBackground
            )
            Text(
                text = state.title,
                style = AppTheme.typography.label,
                color = AppTheme.colors.onBackground
            )
        }
    }
}

// Data classes and default data moved here from ProductNutritionData.kt
data class ProductNutritionState(
    val calories: Calories,
    val nutrition: List<NutritionState>
)

data class Calories(
    val value: String,
    val unit: String
)

data class NutritionState(
    val amount: String,
    val unit: String,
    val title: String
)

val ProductNutritionData = ProductNutritionState(
    calories = Calories(
        value = "650",
        unit = "Cal"
    ),
    nutrition = listOf(
        NutritionState(
            amount = "35",
            unit = "g",
            title = "Total Fat (45% DV)"
        ),
        NutritionState(
            amount = "43",
            unit = "g",
            title = "Total Fat (16% DV)"
        ),
        NutritionState(
            amount = "36",
            unit = "g",
            title = "Protein"
        )
    )
)