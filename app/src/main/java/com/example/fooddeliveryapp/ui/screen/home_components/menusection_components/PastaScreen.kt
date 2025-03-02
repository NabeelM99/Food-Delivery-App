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
import com.example.fooddeliveryapp.ui.screen.getDrawableId
import com.google.firebase.firestore.FirebaseFirestore

data class Pasta(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val productDescription: String = ""
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastaScreen(navController: NavController){
    val pastas = remember { mutableStateListOf<Pasta>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect (Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pastas")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPastas = result.documents.mapNotNull { doc ->
                    try {
                        Pasta(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
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
                PastaCard (pasta = pasta, navController = navController)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastaCard (pasta: Pasta, navController: NavController){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable{
                Log.d("Navigation", "Navigating to pasta with ID: ${pasta.id}")
                navController.navigate("productDetailsScreen/pastas/${pasta.id}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = painterResource(id = getDrawableId(pasta.imageUrl)),
                contentDescription = pasta.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column (
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = pasta.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pasta.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
