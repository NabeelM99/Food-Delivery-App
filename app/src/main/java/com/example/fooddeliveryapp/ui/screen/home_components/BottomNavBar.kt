package com.example.fooddeliveryapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    cartItemCount: Int = 0,
    selectedBurgerId: Int = 0, // Add selected burger ID
    selectedAmount: Int = 1 // Add selected amount
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .zIndex(1f),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Spacer(modifier = Modifier.height(8.dp)) // Add space at the top

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp) // Add padding to move icon down
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentRoute == item.route)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show badge for cart if there are items
                        if (item.route == "cart" && cartItemCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(cartItemCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        modifier = Modifier.padding(top = 0.dp) // Add padding to move label down
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "cart") {
                        onNavigate("cart")
                    } else if (item.route == "search") {
                        onNavigate("SearchBarSection") // Explicitly navigate to the search screen
                    } else if (item.route == "home" && currentRoute != "home") {
                        onNavigate(item.route)
                    } else if (item.route != "home") {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

private val bottomNavItems = listOf(
    NavItem(
        icon = Icons.Default.LocationOn,
        label = "Location",
        route = "location"
    ),
    NavItem(
        icon = Icons.Default.Search,
        label = "Search",
        route = "search"
    ),
    NavItem(
        icon = Icons.Default.Home,
        label = "Home",
        route = "home"
    ),
    NavItem(
        icon = Icons.Default.ShoppingCart,
        label = "My Cart",
        route = "cart" // This route will navigate to AddToCartScreen
    ),
    NavItem(
        icon = Icons.Default.Person,
        label = "Me",
        route = "profileView"
    )
)