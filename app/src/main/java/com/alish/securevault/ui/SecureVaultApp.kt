package com.alish.securevault.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alish.securevault.model.PasswordEntry
import com.alish.securevault.model.RecentVaultItem
import com.alish.securevault.model.SecurityPreference
import com.alish.securevault.model.VaultCategory
import com.alish.securevault.model.VaultItem
import com.alish.securevault.model.VaultStat
import java.util.Locale

private enum class VaultDestination {
    Home, Vault, Passwords, Security
}

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
    var selectedDestination by rememberSaveable { mutableStateOf(VaultDestination.Home) }

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
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ) {
                        VaultDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = selectedDestination == destination,
                                onClick = { selectedDestination = destination },
                                icon = {
                                    Icon(
                                            imageVector = when (destination) {
                                                VaultDestination.Home -> Icons.Default.PhotoLibrary
                                            VaultDestination.Vault -> Icons.Default.Lock
                                                VaultDestination.Passwords -> Icons.Default.CreditCard
                                                VaultDestination.Security -> Icons.Default.Security
                                            },
                                        contentDescription = destination.name
                                    )
                                },
                                label = { Text(destination.name) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (selectedDestination) {
                    VaultDestination.Home -> HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        onLock = viewModel::lock,
                        onImportMedia = onImportMedia,
                        onImportFiles = onImportFiles,
                        onImportAudio = onImportAudio
                    )

                    VaultDestination.Vault -> VaultScreen(
                        modifier = Modifier.padding(innerPadding),
                        items = uiState.vaultItems,
                        onImportMedia = onImportMedia,
                        onImportFiles = onImportFiles,
                        onImportAudio = onImportAudio,
                        onOpenVaultItem = onOpenVaultItem
                    )

                    VaultDestination.Passwords -> PasswordsScreen(
                        modifier = Modifier.padding(innerPadding),
                        entries = uiState.passwordEntries,
                        onAddPassword = viewModel::addPassword,
                        onRevealToggle = viewModel::toggleReveal,
                        revealedSecret = viewModel::revealedSecret
                    )

                    VaultDestination.Security -> SecurityScreen(
                        modifier = Modifier.padding(innerPadding),
                        preferences = uiState.securityPreferences,
                        statusMessage = uiState.statusMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun LockScreen(
    onUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF081421),
                        Color(0xFF10253A),
                        Color(0xFF1C3F63)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShieldMoon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SecureVault",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Import real photos, videos, files, audio, and passwords into private encrypted local storage.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.78f)
            )
            Spacer(modifier = Modifier.height(28.dp))
            FilledTonalButton(onClick = onUnlock) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Unlock with Biometrics")
            }
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    uiState: SecureVaultUiState,
    onLock: () -> Unit,
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            DashboardHeader(onLock = onLock)
        }
        item {
            VaultHeroCard(
                isLoading = uiState.isLoading,
                isImporting = uiState.isImporting,
                statusMessage = uiState.statusMessage
            )
        }
        item {
            ImportActionsCard(
                onImportMedia = onImportMedia,
                onImportFiles = onImportFiles,
                onImportAudio = onImportAudio
            )
        }
        item {
            Text(
                text = "Vault Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(uiState.vaultStats) { stat ->
            VaultStatCard(stat = stat)
        }
        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        if (uiState.recentItems.isEmpty()) {
            item {
                EmptyCard(
                    title = "Vault is ready",
                    body = "Import media, documents, or audio to start building your encrypted library."
                )
            }
        } else {
            items(uiState.recentItems) { recent ->
                RecentItemCard(item = recent)
            }
        }
    }
}

@Composable
private fun VaultScreen(
    modifier: Modifier,
    items: List<VaultItem>,
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenVaultItem: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Encrypted vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            ImportActionsCard(
                onImportMedia = onImportMedia,
                onImportFiles = onImportFiles,
                onImportAudio = onImportAudio
            )
        }

        VaultCategory.entries.filterNot { it == VaultCategory.Passwords }.forEach { category ->
            val categoryItems = items.filter { it.category == category }
            item {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (categoryItems.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No ${category.name.lowercase(Locale.getDefault())} yet",
                        body = "Imported ${category.name.lowercase(Locale.getDefault())} will appear here after encryption."
                    )
                }
            } else {
                items(categoryItems, key = { it.id }) { item ->
                    VaultItemCard(item = item, onOpen = { onOpenVaultItem(item.id) })
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    onLock: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Private by design",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your secure digital vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        FilledTonalButton(onClick = onLock) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Lock")
        }
    }
}

@Composable
private fun VaultHeroCard(
    isLoading: Boolean,
    isImporting: Boolean,
    statusMessage: String?
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0E1A2B),
                            Color(0xFF193B58),
                            Color(0xFF197278)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Zero-compromise privacy",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isImporting) {
                        "Import in progress. Selected items are being encrypted into app-private local storage."
                    } else {
                        "Use Android pickers to bring in real content, then keep it encrypted in local app storage outside shared galleries."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.84f)
                )
                Text(
                    text = when {
                        isLoading -> "Vault status: loading"
                        statusMessage != null -> statusMessage
                        else -> "Vault status: ready"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD166)
                )
            }
        }
    }
}

@Composable
private fun ImportActionsCard(
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Import into vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onImportMedia, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Import photos and videos")
            }
            OutlinedButton(onClick = onImportFiles, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Import files")
            }
            OutlinedButton(onClick = onImportAudio, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AudioFile, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Import audio")
            }
        }
    }
}

@Composable
private fun VaultStatCard(
    stat: VaultStat
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = stat.accent.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(stat.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.category.icon(),
                    contentDescription = null,
                    tint = stat.accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stat.itemCount} items",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = stat.label,
                style = MaterialTheme.typography.labelLarge,
                color = stat.accent
            )
        }
    }
}

@Composable
private fun RecentItemCard(
    item: RecentVaultItem
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.category.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.category.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    onOpen: () -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.mimeType} - ${formatSize(item.sizeBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onOpen) {
                    Icon(Icons.Outlined.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Open")
                }
            }
            Text(
                text = "Imported ${item.importedAt}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PasswordsScreen(
    modifier: Modifier,
    entries: List<PasswordEntry>,
    onAddPassword: (String, String, String) -> Unit,
    onRevealToggle: (String) -> Unit,
    revealedSecret: (PasswordEntry) -> String
) {
    var label by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var secret by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Password vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            Card(shape = RoundedCornerShape(26.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add a new secret",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Account name") }
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username or email") }
                    )
                    OutlinedTextField(
                        value = secret,
                        onValueChange = { secret = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") }
                    )
                    Button(
                        onClick = {
                            onAddPassword(label, username, secret)
                            label = ""
                            username = ""
                            secret = ""
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text("Save encrypted entry")
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            item {
                EmptyCard(
                    title = "No saved passwords yet",
                    body = "Add your first encrypted credential and it will be stored in local private app storage."
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                PasswordEntryCard(
                    entry = entry,
                    secret = revealedSecret(entry),
                    onRevealToggle = { onRevealToggle(entry.id) }
                )
            }
        }
    }
}

@Composable
private fun PasswordEntryCard(
    entry: PasswordEntry,
    secret: String,
    onRevealToggle: () -> Unit
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRevealToggle) {
                    Icon(
                        imageVector = if (secret == "Tap reveal to decrypt") {
                            Icons.Outlined.Visibility
                        } else {
                            Icons.Outlined.VisibilityOff
                        },
                        contentDescription = null
                    )
                }
            }
            HorizontalDivider()
            Text(
                text = secret,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.updatedAt,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SecurityScreen(
    modifier: Modifier,
    preferences: List<SecurityPreference>,
    statusMessage: String?
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Security center",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Current security posture",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Imported files are encrypted into private storage, passwords are kept encrypted at rest, and opened files are only decrypted into temporary cache on demand.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        if (statusMessage != null) {
            item {
                EmptyCard(
                    title = "Latest activity",
                    body = statusMessage
                )
            }
        }
        items(preferences) { preference ->
            SecurityPreferenceCard(preference = preference)
        }
    }
}

@Composable
private fun SecurityPreferenceCard(
    preference: SecurityPreference
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preference.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = preference.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (preference.enabled) "ON" else "OFF",
                style = MaterialTheme.typography.labelLarge,
                color = if (preference.enabled) Color(0xFF14866D) else Color(0xFF8A5A44)
            )
        }
    }
}

@Composable
private fun EmptyCard(
    title: String,
    body: String
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun VaultCategory.icon() = when (this) {
    VaultCategory.Photos -> Icons.Default.PhotoLibrary
    VaultCategory.Videos -> Icons.Default.Videocam
    VaultCategory.Files -> Icons.Default.Description
    VaultCategory.Audio -> Icons.Default.AudioFile
    VaultCategory.Passwords -> Icons.Default.CreditCard
}

private fun formatSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "Unknown size"
    val kb = 1024L
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        sizeBytes >= gb -> String.format(Locale.getDefault(), "%.2f GB", sizeBytes.toDouble() / gb)
        sizeBytes >= mb -> String.format(Locale.getDefault(), "%.2f MB", sizeBytes.toDouble() / mb)
        sizeBytes >= kb -> String.format(Locale.getDefault(), "%.1f KB", sizeBytes.toDouble() / kb)
        else -> "$sizeBytes B"
    }
}
