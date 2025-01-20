package com.example.fooddeliveryapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
    onNavigate: (String) -> Unit
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
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp)
                        )
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
                    if (item.route == "home" && currentRoute != "home") {
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
        icon = Icons.Default.Home,
        label = "Home",
        route = "home"
    ),
    NavItem(
        icon = Icons.Default.ShoppingCart,
        label = "My Cart",
        route = "cart"
    ),
    NavItem(
        icon = Icons.Default.Person,
        label = "Me",
        route = "profile"
    )
)