package com.example.fooddeliveryapp.ui.screen.home_components.menusection_components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.LinkAnnotation
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

data class Juices(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val productDescription: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuicesScreen(navController: NavController){
    val juices = remember { mutableStateListOf<Juices>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect (Unit){
        val db = FirebaseFirestore.getInstance()
        db.collection("juices")
            .get()
            .addOnSuccessListener { result ->
                val fetchedJuices = result.documents.mapNotNull { doc ->
                    try {
                        Juices(
                            id = doc.getLong("id")?.toInt()?:0,
                            name = doc.getString("name")?."",
                            price
                        )
                    }
                }
            }
    }

}