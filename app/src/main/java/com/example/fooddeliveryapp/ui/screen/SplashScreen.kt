package com.example.fooddeliveryapp.ui.screen

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddeliveryapp.MainActivity
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.theme.extendedTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 4-second delay
        onNavigateToNext() // Navigate to the next screen
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.food_logo1),
                contentDescription = "App Logo",
                modifier = Modifier.size(250.dp)

            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "CULINARIO",
                style = extendedTypography.titleMedium,
                textAlign = TextAlign.Center,
                fontSize = 40.sp
            )



            Text(
                text = "Created by Nabeel",
                style = extendedTypography.bodySmall,  // Use smaller font style from Typography
                modifier = Modifier
                    .padding(top = 100.dp)
            )
        }
    }
}

