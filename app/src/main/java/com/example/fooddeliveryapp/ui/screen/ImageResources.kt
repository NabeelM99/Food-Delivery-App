package com.example.fooddeliveryapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.fooddeliveryapp.R

@Composable
fun getDrawableId(imageName: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(
        imageName, // Must match EXACTLY with your drawable file names
        "drawable",
        context.packageName
    ).takeIf { it != 0 } ?: R.drawable.img_placeholder
}