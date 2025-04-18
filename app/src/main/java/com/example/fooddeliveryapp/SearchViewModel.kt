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
    private val collections = listOf(
        "burgers",
        "drinks",
        "fries",
        "pastas",
        "juices"
    )

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("SEARCH", "Init search for: '$query'")

                if (query.isEmpty()) {
                    _searchResults.value = emptyList()
                    return@launch
                }

                val cleanQuery = query.lowercase().trim()
                val results = mutableListOf<Product>()

                for (collection in collections) {
                    try {
                        Log.d("SEARCH", "Searching in $collection")

                        /*val querySnapshot = db.collection(collection)
                            .whereGreaterThanOrEqualTo("name_lowercase", cleanQuery)
                            .whereLessThanOrEqualTo("name_lowercase", "$cleanQuery\uF8FF")
                            .get()
                            .await()

                        Log.d("SEARCH", "Found ${querySnapshot.size()} in $collection")*/

                        val querySnapshot = db.collection(collection)
                            .get()
                            .await()

                        /*
                        val querySnapshot = db.collection(collection)
                        .get()
                        .await()
                        .documents
                        .filter { doc ->
                            (doc.getString("name") ?: "").lowercase().contains(cleanQuery)
                        }
                    */

                        querySnapshot.documents.forEach { doc ->
                            try {
                                val name = doc.getString("name") ?: ""
                                // Case-insensitive contains check
                                if (name.lowercase().contains(cleanQuery)) {
                                    val product = Product(
                                        id = doc.id,
                                        name = name,
                                        description = doc.getString("description") ?: "",
                                        price = doc.getDouble("price") ?: 0.0,
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        type = collection,
                                        productDescription = doc.getString("productDescription") ?: ""
                                    )
                                    results.add(product)
                                    Log.d("SEARCH", "Matched: ${product.name}")
                                }
                            } catch (e: Exception) {
                                Log.e("SEARCH", "Error parsing doc ${doc.id}: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SEARCH", "Error searching $collection: ${e.message}")
                    }
                }

                _searchResults.value = results.distinctBy { it.id }
                Log.d("SEARCH", "Total results: ${results.size}")
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                Log.e("SEARCH", "Search error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}