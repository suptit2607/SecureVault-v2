package com.alish.securevault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alish.securevault.model.RecentVaultItem
import com.alish.securevault.model.VaultStat
import com.alish.securevault.ui.SecureVaultUiState
import com.alish.securevault.ui.theme.NeonCyan
import com.alish.securevault.ui.theme.Sapphire700
import com.alish.securevault.ui.theme.Sapphire800

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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            DashboardHeader(onLock = onLock)
        }

        item {
            VaultHealthCard(uiState)
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = NeonCyan
            )
        }

        item {
            BentoQuickActions(
                onImportMedia = onImportMedia,
                onImportFiles = onImportFiles,
                onImportAudio = onImportAudio
            )
        }

        item {
            Text(
                text = "Recent Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.White
            )
        }

        if (uiState.recentItems.isEmpty()) {
            item {
                EmptyStateCard()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SecureVault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Elite Encryption Active",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan
            )
        }
        IconButton(
            onClick = onLock,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = NeonCyan)
        }
    }
}

@Composable
private fun VaultHealthCard(uiState: SecureVaultUiState) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Sapphire800)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Vault Status: Secure", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.35f }, // Mock storage usage
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = NeonCyan,
                trackColor = Sapphire700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "3.2 GB of 10 GB Encrypted Storage used",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun BentoQuickActions(
    onImportMedia: () -> Unit,
    onImportFiles: () -> Unit,
    onImportAudio: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large Media Box
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(NeonCyan.copy(alpha = 0.3f), Sapphire700.copy(alpha = 0.8f))
                    )
                )
                .clickable { onImportMedia() }
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Media", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text("Photos & Videos", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }

        // Vertical stack for others
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Sapphire800)
                    .clickable { onImportFiles() }
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Files", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Sapphire800)
                    .clickable { onImportAudio() }
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RecentItemCard(item: RecentVaultItem) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Sapphire800.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Sapphire700),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.category.icon(), contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(item.meta, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Sapphire800.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Text("No recent activity", color = Color.White.copy(alpha = 0.3f))
    }
}
