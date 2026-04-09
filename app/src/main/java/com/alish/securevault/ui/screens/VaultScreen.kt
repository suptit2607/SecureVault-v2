package com.alish.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alish.securevault.model.VaultCategory
import com.alish.securevault.model.VaultItem
import java.util.Locale

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    items: List<VaultItem>,
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenVaultItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit
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
            ImportActionsCardSmall(
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
                    VaultItemCard(
                        item = item,
                        onOpen = { onOpenVaultItem(item.id) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportActionsCardSmall(
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = onImportMedia, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Media", style = MaterialTheme.typography.labelSmall)
            }
            FilledTonalButton(onClick = onImportFiles, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Files", style = MaterialTheme.typography.labelSmall)
            }
            FilledTonalButton(onClick = onImportAudio, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Audio", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    FilledTonalButton(onClick = onOpen) {
                        Icon(Icons.Outlined.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Open")
                    }
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
