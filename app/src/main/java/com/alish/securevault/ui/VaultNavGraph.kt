package com.alish.securevault.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alish.securevault.ui.screens.HomeScreen
import com.alish.securevault.ui.screens.PasswordsScreen
import com.alish.securevault.ui.screens.SecurityScreen
import com.alish.securevault.ui.screens.VaultScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Vault : Screen("vault")
    object Passwords : Screen("passwords")
    object Security : Screen("security")
}

@Composable
fun VaultNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: SecureVaultUiState,
    viewModel: SecureVaultViewModel,
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenVaultItem: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onLock = viewModel::lock,
                onImportMedia = onImportMedia,
                onImportFiles = onImportFiles,
                onImportAudio = onImportAudio
            )
        }
        composable(Screen.Vault.route) {
            VaultScreen(
                items = uiState.vaultItems,
                onImportMedia = onImportMedia,
                onImportFiles = onImportFiles,
                onImportAudio = onImportAudio,
                onOpenVaultItem = onOpenVaultItem,
                onDeleteItem = viewModel::deleteItem
            )
        }
        composable(Screen.Passwords.route) {
            PasswordsScreen(
                entries = uiState.passwordEntries,
                onAddPassword = viewModel::addPassword,
                onRevealToggle = viewModel::toggleReveal,
                revealedSecret = viewModel::revealedSecret,
                onDeletePassword = viewModel::deletePassword
            )
        }
        composable(Screen.Security.route) {
            SecurityScreen(
                preferences = uiState.securityPreferences,
                statusMessage = uiState.statusMessage
            )
        }
    }
}
