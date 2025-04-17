package com.example.fooddeliveryapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val collections = listOf("burgers", "drinks", "fries", "pastas", "juices")

    fun searchProducts(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val results = mutableListOf<Product>()
                val searchQuery = query.lowercase()

                // Search across all collections
                collections.forEach { collection ->
                    val snapshot = db.collection(collection)
                        .whereGreaterThanOrEqualTo("name_lowercase", searchQuery)
                        .whereLessThanOrEqualTo("name_lowercase", searchQuery + "\uf8ff")
                        .get()
                        .await()

                    results.addAll(snapshot.documents.mapNotNull { doc ->
                        try {
                            Product(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                price = doc.getDouble("price") ?: 0.0,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                type = collection,
                                productDescription = doc.getString("productDescription") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    })
                }

                _searchResults.value = results.distinctBy { it.id }
                Log.d("Search", "Found ${results.size} results")
            } catch (e: Exception) {
                _error.value = "Error searching products: ${e.message}"
                Log.e("Search", "Search failed", e)
            } finally {
                _isLoading.value = false
                Log.d("Search", "Search completed")
            }
        }
    }
}