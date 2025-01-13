package com.example.fooddeliveryapp.data

import androidx.annotation.DrawableRes
import com.example.fooddeliveryapp.R

data class ProductFlavorState(
    val name: String,
    val price: String,
    @DrawableRes val imgRes: Int
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
    ),
)