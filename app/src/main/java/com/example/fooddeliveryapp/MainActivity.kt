package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.fooddeliveryapp.data.OrderState
import com.example.fooddeliveryapp.ui.screen.ProductDetailsScreen
import com.example.fooddeliveryapp.ui.theme.AppTheme

private const val PRODUCT_PRICE_PER_UNIT = 5.25
private const val PRODUCT_CURRENCY = "$"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                var amount by remember { mutableIntStateOf(5) }
                val totalPrice by remember { derivedStateOf { amount * PRODUCT_PRICE_PER_UNIT }}

                ProductDetailsScreen(
                    onCheckOutClicked = {},
                    orderState = OrderState(
                        amount = amount,
                        totalPrice = "$PRODUCT_CURRENCY$totalPrice"
                    ),
                    onAddItemClicked = {
                        amount = amount + 1 },
                    onRemoveItemClicked = {
                        if (amount > 0) {
                            amount = amount - 1

                        } }
                )
            }
        }
    }
}