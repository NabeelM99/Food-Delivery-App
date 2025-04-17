/*package com.example.fooddeliveryapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel() {
    private val _searchResults = mutableStateOf(SearchState())
    val searchResults: SearchState by _searchResults

    fun searchProducts(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = SearchState(data = emptyList())
            return
        }

        _searchResults.value = SearchState(isLoading = true)

        viewModelScope.launch {
            try {
                val results = mutableListOf<Product>()
                val collections = listOf("burgers", "drinks") // Add more collections as needed

                collections.forEach { collection ->
                    val snapshot = FirebaseFirestore.getInstance()
                        .collection(collection)
                        .whereGreaterThanOrEqualTo("name", query)
                        .whereLessThanOrEqualTo("name", query + "\uf8ff")
                        .get()
                        .await()

                    results.addAll(snapshot.documents.map { doc ->
                        com.example.fooddeliveryapp.ui.screen.components.Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            type = collection
                        )
                    })
                }

                _searchResults.value = SearchState(
                    data = results.distinctBy { it.id },
                    isLoading = false
                )
            } catch (e: Exception) {
                _searchResults.value = SearchState(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }
}

data class SearchState(
    val data: List<Product>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val type: String
)
*/
