package com.example.fooddeliveryapp.ui.screen.components


data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val type: String,
    val productDescription: String = ""
)

