package com.alish.securevault.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class VaultCategory {
    Photos,
    Videos,
    Files,
    Audio,
    Passwords
}

fun VaultCategory.icon(): ImageVector = when (this) {
    VaultCategory.Photos -> Icons.Default.PhotoCamera
    VaultCategory.Videos -> Icons.Default.VideoLibrary
    VaultCategory.Files -> Icons.Default.Description
    VaultCategory.Audio -> Icons.Default.AudioFile
    VaultCategory.Passwords -> Icons.Default.Lock
}

data class VaultItem(
    val id: String,
    val displayName: String,
    val category: VaultCategory,
    val mimeType: String,
    val sizeBytes: Long,
    val encryptedFileName: String,
    val importedAt: String
)

data class VaultStat(
    val category: VaultCategory,
    val itemCount: Int,
    val label: String,
    val accent: Color
)

data class RecentVaultItem(
    val id: String,
    val title: String,
    val category: VaultCategory,
    val meta: String
)

data class OpenVaultAsset(
    val fileName: String,
    val mimeType: String,
    val contentUri: String
)

data class PasswordEntry(
    val id: String,
    val label: String,
    val username: String,
    val secret: String,
    val updatedAt: String
)

data class SecurityPreference(
    val title: String,
    val description: String,
    val enabled: Boolean
)
