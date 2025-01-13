package com.example.fooddeliveryapp.data

import android.text.Highlights
import androidx.annotation.DrawableRes
import com.example.fooddeliveryapp.R

data class ProductHighLightState(
    val text: String,
    val type: ProductHighLightType
)


enum class ProductHighLightType{
    PRIMARY, SECONDARY
}

data class ProductPreviewState(
    val headline: String = "Mr. Burger",
    @DrawableRes val productImg: Int = R.drawable.img_burger,
    val highlights: List<ProductHighLightState> = listOf(
        ProductHighLightState(
            text = "Classic Taste",
            type = ProductHighLightType.SECONDARY
        ),
        ProductHighLightState(
            text = "Bestseller",
            type = ProductHighLightType.PRIMARY
        )
    )
    )