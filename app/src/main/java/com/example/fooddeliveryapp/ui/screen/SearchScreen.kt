package com.example.fooddeliveryapp.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.SearchViewModel
import com.example.fooddeliveryapp.ui.screen.components.ProductCard
import kotlinx.coroutines.delay

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val searchViewModel: SearchViewModel = viewModel()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(400)
            searchViewModel.searchProducts(searchQuery)
        } else {
            //searchViewModel.searchResults.value = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search foods and drinks...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            when {
                searchViewModel.isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                searchViewModel.error.value != null -> {
                    Text(
                        text = searchViewModel.error.value!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }

                searchViewModel.searchResults.value.isEmpty() ->
                    Text(
                        text = if (searchQuery.isEmpty())
                            "Start typing to search"
                        else
                            "No results found for \"$searchQuery\"",
                        modifier = Modifier
                            .align(Alignment.Center)
                    )


                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchViewModel.searchResults.value) { product ->
                            ProductCard(
                                product = product,
                                productType = product.type,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}