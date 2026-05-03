package com.pm.foodscanner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pm.foodscanner.ui.favorites.FavoritesScreen
import com.pm.foodscanner.ui.favorites.FavoritesViewModel
import com.pm.foodscanner.ui.history.HistoryScreen
import com.pm.foodscanner.ui.history.HistoryViewModel
import com.pm.foodscanner.ui.home.HomeScreen
import com.pm.foodscanner.ui.profiletab.ProfileTabScreen
import com.pm.foodscanner.ui.profiletab.ProfileTabViewModel

@Composable
fun MainScreen(
    onScanBarcode: () -> Unit,
    onScanMeal: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) }

    val historyVm: HistoryViewModel = hiltViewModel()
    val favoritesVm: FavoritesViewModel = hiltViewModel()
    val profileVm: ProfileTabViewModel = hiltViewModel()

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> historyVm.reloadHistory()
            2 -> favoritesVm.loadFavorites()
            3 -> profileVm.loadData()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HistoryScreen(viewModel = historyVm)
                1 -> HomeScreen(onScanBarcode = onScanBarcode, onScanMeal = onScanMeal)
                2 -> FavoritesScreen(viewModel = favoritesVm)
                3 -> ProfileTabScreen(
                    viewModel = profileVm,
                    onEditProfile = onEditProfile,
                    onLogout = onLogout
                )
            }
        }
    }
}
