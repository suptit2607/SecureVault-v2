package com.alish.securevault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alish.securevault.model.RecentVaultItem
import com.alish.securevault.model.VaultStat
import com.alish.securevault.ui.SecureVaultUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
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
private fun DashboardHeader(onLock: () -> Unit) {
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
private fun VaultStatCard(stat: VaultStat) {
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
private fun RecentItemCard(item: RecentVaultItem) {
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
