package com.example.fooddeliveryapp.ui.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun ProductDescriptionSection(
    modifier: Modifier = Modifier,
    productDescription: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(11.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Description",
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onBackground
        )
        Text(
            text = productDescription,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onBackground,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Data moved here from ProductDescriptionData.kt
val ProductDescriptionData = "Introducing Mr. Burger: the ultimate app for a burger lover's dream!\n" +
        "\n" +
        "Sink your teeth into a succulent beef patty, smothered in a rich, melting blend of cheddar, Swiss and American cheeses. Crispy halal bacon\n" +
        "\n" +
        "Drizzled with zesty BBQ sauce and tucked inside a perfectly toasted brioche bun, MR. Burger is pure, cheesy bliss in every bite."