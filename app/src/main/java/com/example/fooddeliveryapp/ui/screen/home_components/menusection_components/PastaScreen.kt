package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.example.fooddeliveryapp.ui.screen.components.ProductCard
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastaScreen(navController: NavController){
    val pastas = remember { mutableStateListOf<Product>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect (Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pastas")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPastas = result.documents.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            type = "pastas",
                            productDescription = doc.getString("productDescription") ?: "",
                        )
                    } catch (e: Exception){
                        Log.e("Firestore", "Error parsing Pasta: ${doc.id}", e)
                        null
                    }
                }
                pastas.clear()
                pastas.addAll(fetchedPastas)
                loading = false
            }
            .addOnFailureListener{ e ->
                Log.e("Firestore", "Error fetching pastas", e)
                loading = false
            }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Pastas") },
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA500)
                )
            )
        }
    ){ paddingValues ->
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ){
            items(pastas) { pasta ->
                //PastaCard (pasta = pasta, navController = navController)
                ProductCard(
                    product = pasta,
                    productType = "pastas",
                    navController = navController
                )
            }
        }
    }
}
