package com.alish.securevault.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alish.securevault.data.PasswordVaultRepository
import com.alish.securevault.data.VaultRepository
import com.alish.securevault.model.OpenVaultAsset
import com.alish.securevault.model.PasswordEntry
import com.alish.securevault.model.RecentVaultItem
import com.alish.securevault.model.SecurityPreference
import com.alish.securevault.model.VaultItem
import com.alish.securevault.model.VaultStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecureVaultUiState(
    val isUnlocked: Boolean = false,
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val vaultStats: List<VaultStat> = emptyList(),
    val recentItems: List<RecentVaultItem> = emptyList(),
    val vaultItems: List<VaultItem> = emptyList(),
    val passwordEntries: List<PasswordEntry> = emptyList(),
    val securityPreferences: List<SecurityPreference> = emptyList(),
    val revealedEntryId: String? = null,
    val statusMessage: String? = null
)

class SecureVaultViewModel(
    private val vaultRepository: VaultRepository,
    private val passwordVaultRepository: PasswordVaultRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SecureVaultUiState(
            securityPreferences = vaultRepository.securityPreferences()
        )
    )
    val uiState: StateFlow<SecureVaultUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun unlock() {
        _uiState.update { it.copy(isUnlocked = true) }
    }

    fun lock() {
        _uiState.update { it.copy(isUnlocked = false, revealedEntryId = null) }
    }

    fun addPassword(label: String, username: String, secret: String) {
        if (label.isBlank() || username.isBlank() || secret.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Fill in every password field before saving.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val entries = passwordVaultRepository.addEntry(label, username, secret)
                val vaultItems = _uiState.value.vaultItems
                _uiState.update {
                    it.copy(
                        passwordEntries = entries,
                        revealedEntryId = null,
                        statusMessage = "Password saved securely."
                    )
                }
                refreshStatsAndRecents(vaultItems = vaultItems, passwordEntries = entries)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(statusMessage = "Could not save the password entry.")
                }
            }
        }
    }

    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, statusMessage = "Encrypting ${uris.size} item(s)...") }
            runCatching {
                val vaultItems = vaultRepository.importFromUris(uris)
                _uiState.update {
                    it.copy(
                        vaultItems = vaultItems,
                        isImporting = false,
                        statusMessage = "Import complete."
                    )
                }
                refreshStatsAndRecents(vaultItems = vaultItems, passwordEntries = _uiState.value.passwordEntries)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isImporting = false,
                        statusMessage = "Import failed. Please try again."
                    )
                }
            }
        }
    }

    fun toggleReveal(entryId: String) {
        _uiState.update { state ->
            state.copy(revealedEntryId = if (state.revealedEntryId == entryId) null else entryId)
        }
    }

    fun revealedSecret(entry: PasswordEntry): String {
        return if (_uiState.value.revealedEntryId == entry.id) {
            passwordVaultRepository.revealSecret(entry)
        } else {
            "Tap reveal to decrypt"
        }
    }

    suspend fun prepareOpenAsset(itemId: String): OpenVaultAsset? {
        return vaultRepository.prepareOpenAsset(itemId)
    }

    private fun refresh() {
        viewModelScope.launch {
            runCatching {
                val vaultItems = vaultRepository.getVaultItems()
                val passwordEntries = passwordVaultRepository.getEntries()
                _uiState.update {
                    it.copy(
                        vaultItems = vaultItems,
                        passwordEntries = passwordEntries,
                        isLoading = false
                    )
                }
                refreshStatsAndRecents(vaultItems = vaultItems, passwordEntries = passwordEntries)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        statusMessage = "Could not load existing vault data."
                    )
                }
            }
        }
    }

    private suspend fun refreshStatsAndRecents(
        vaultItems: List<VaultItem>,
        passwordEntries: List<PasswordEntry>
    ) {
        val passwordCount = passwordEntries.size
        val stats = vaultRepository.vaultStats(passwordCount)
        val recents = vaultRepository.recentItems(passwordCount)
        _uiState.update {
            it.copy(
                vaultStats = stats,
                recentItems = recents,
                vaultItems = vaultItems,
                passwordEntries = passwordEntries
            )
        }
    }

    companion object {
        fun factory(
            vaultRepository: VaultRepository,
            passwordVaultRepository: PasswordVaultRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SecureVaultViewModel(
                    vaultRepository = vaultRepository,
                    passwordVaultRepository = passwordVaultRepository
                ) as T
            }
        }
    }
}
