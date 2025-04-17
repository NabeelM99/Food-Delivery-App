/*package com.example.fooddeliveryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fooddeliveryapp.ui.screen.components.Product
import com.example.fooddeliveryapp.SearchViewModel
import com.example.fooddeliveryapp.ui.screen.home_components.SearchBarSection

@Composable
fun SearchScreen(navController: NavController) {
    val searchViewModel: SearchViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBarSection(
            searchText = searchQuery,
            onSearchTextChange = {
                searchQuery = it
                searchViewModel.searchProducts(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            searchResults.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
            !searchResults.error.isNullOrEmpty() -> {
                Text("Error: ${searchResults.error}")
            }
            searchResults.data.isNullOrEmpty() -> {
                Text("No results found", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
            }
            else -> {
                SearchResultsList(
                    products = searchResults.data!!,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun SearchResultsList(products: List<Product>, navController: NavController) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(products) { product ->
            ProductSearchItem(
                product = product,
                onClick = {
                    navController.navigate("productDetailsScreen/${product.type}/${product.id}")
                }
            )
        }
    }
}

@Composable
private fun ProductSearchItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
        }
    }
}*/
