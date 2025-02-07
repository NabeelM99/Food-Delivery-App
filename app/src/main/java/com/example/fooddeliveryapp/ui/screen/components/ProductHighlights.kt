/*package com.example.fooddeliveryapp.ui.screen.components

import android.text.Highlights
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MovableContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
//import com.example.fooddeliveryapp.data.ProductHighLightState
//import com.example.fooddeliveryapp.data.ProductHighLightType
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun ProductHighLights(
    modifier: Modifier = Modifier,
    highlights: List<ProductHighLightState>
    ){
        Column (
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ){
            highlights.onEach { item ->
            Highlight(
                text = item.text,
                colors = HighlightDefaults.colors(type = item.type)
            )
            }
        }
}

@Composable
private fun Highlight(
    modifier: Modifier = Modifier,
    text: String,
    colors: HighlightColors = HighlightDefaults.defaultColors
){
    Surface (
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        contentColor = colors.contentColor,
        color = colors.containerColor
    ){
        Box(
            modifier = Modifier.padding(
                vertical = 10.dp,
                horizontal = 12.dp
            )
        ){
            Text(
                text =text,
                style = AppTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private object HighlightDefaults{
    val defaultColors = HighlightColors(
        contentColor = Color.Unspecified,
        containerColor = Color.Unspecified
    )

    @Composable
    fun colors(type: ProductHighLightType): HighlightColors = when (type){
        ProductHighLightType.PRIMARY -> HighlightColors(
            containerColor = AppTheme.colors.highlightSurface,
            contentColor = AppTheme.colors.onHighLightSurface
        )

        ProductHighLightType.SECONDARY -> HighlightColors(
            containerColor = AppTheme.colors.actionSurface,
            contentColor = AppTheme.colors.onActionSurface
        )

    }
}

@Immutable
private data class HighlightColors(
    val containerColor: Color,
    val contentColor: Color
)*/
