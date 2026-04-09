package com.alish.securevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alish.securevault.ui.screens.LockScreen
import com.alish.securevault.ui.theme.NeonCyan
import com.alish.securevault.ui.theme.Sapphire700
import com.alish.securevault.ui.theme.Sapphire800

@Composable
fun SecureVaultApp(
    viewModel: SecureVaultViewModel,
    onUnlockRequest: (onSuccess: () -> Unit) -> Unit,
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenVaultItem: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(
            targetState = uiState.isUnlocked,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            label = "vault-entry"
        ) { isUnlocked ->
            if (!isUnlocked) {
                LockScreen(onUnlock = { onUnlockRequest(viewModel::unlock) })
                return@Crossfade
            }

            Scaffold(
                containerColor = Color.Transparent, // Use background from Surface
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = Sapphire800.copy(alpha = 0.96f),
                        tonalElevation = 8.dp
                    ) {
                        val items = listOf(
                            NavigationItem(Screen.Home.route, "Home", Icons.Default.PhotoLibrary),
                            NavigationItem(Screen.Vault.route, "Vault", Icons.Default.Lock),
                            NavigationItem(Screen.Passwords.route, "Passwords", Icons.Default.CreditCard),
                            NavigationItem(Screen.Security.route, "Security", Icons.Default.Security)
                        )
                        
                        items.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    Icon(
                                        item.icon, 
                                        contentDescription = item.label,
                                        tint = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.5f)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        item.label, 
                                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.5f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Sapphire700
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                VaultNavGraph(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    uiState = uiState,
                    viewModel = viewModel,
                    onImportMedia = onImportMedia,
                    onImportFiles = onImportFiles,
                    onImportAudio = onImportAudio,
                    onOpenVaultItem = onOpenVaultItem
                )
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
