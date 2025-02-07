package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fooddeliveryapp.ui.screen.components.*
import com.example.fooddeliveryapp.ui.theme.AppTheme

@Composable
fun ProductDetailsScreen(
    burgerId: Int,
    modifier: Modifier.Companion = Modifier,
    productPreviewState: ProductPreviewState = ProductPreviewState(),
    productFlavors: List<ProductFlavorState> = ProductFlavorData,
    productNutritionState: ProductNutritionState = ProductNutritionData,
    productDescription: String = ProductDescriptionData
) {
    // Manage OrderState internally
    var orderState by remember {
        mutableStateOf(OrderState(amount = 1, totalPrice = "$5.25"))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(0.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main content
        Content(
            productPreviewState = productPreviewState,
            productFlavors = productFlavors,
            productNutritionState = productNutritionState,
            productDescription = productDescription
        )

        // Order Action Bar
        OrderActionBar(
            state = orderState,
            onAddItemClicked = {
                val newAmount = orderState.amount + 1
                orderState = orderState.copy(
                    amount = newAmount,
                    totalPrice = "$${newAmount * 5.25}" // Update total price based on amount
                )
            },
            onRemoveItemClicked = {
                if (orderState.amount > 1) {
                    val newAmount = orderState.amount - 1
                    orderState = orderState.copy(
                        amount = newAmount,
                        totalPrice = "$${newAmount * 5.25}" // Update total price based on amount
                    )
                }
            },
            onCheckOutClicked = {
                // Handle checkout logic (e.g., navigate to the checkout screen)
            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    productPreviewState: ProductPreviewState,
    productFlavors: List<ProductFlavorState>,
    productNutritionState: ProductNutritionState,
    productDescription: String
) {
    val scrollableState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth() // Ensure the column spans the full width
            .verticalScroll(scrollableState)
            .padding(0.dp)
    ) {
        ProductPreviewSection(
            state = productPreviewState,
            modifier = Modifier.fillMaxWidth() // Make the section span the width
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlavorSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            data = productFlavors
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProductNutritionSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = productNutritionState
        )
        Spacer(modifier = Modifier.height(32.dp))
        ProductDescriptionSection(
            productDescription = productDescription,
            modifier = Modifier
                .fillMaxWidth() // Ensure the description section spans the full width
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 128.dp)
        )
    }
}
