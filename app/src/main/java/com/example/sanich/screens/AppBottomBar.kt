package com.example.sanich.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.sanich.navigation.Screen

private val BrandYellow = Color(0xFFFFC400)
private val BrandBlack = Color(0xFF171717)

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = BrandBlack) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Главная") },
            colors = bottomColors()
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Technologist.route,
            onClick = { onNavigate(Screen.Technologist.route) },
            icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
            label = { Text("Технолог") },
            colors = bottomColors()
        )
        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = { onNavigate(Screen.History.route) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("История") },
            colors = bottomColors()
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Reference.route,
            onClick = { onNavigate(Screen.Reference.route) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            label = { Text("Справочник") },
            colors = bottomColors()
        )
    }
}

@Composable
private fun bottomColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandBlack,
    selectedTextColor = BrandYellow,
    indicatorColor = BrandYellow,
    unselectedIconColor = Color.White.copy(alpha = 0.75f),
    unselectedTextColor = Color.White.copy(alpha = 0.75f)
)
