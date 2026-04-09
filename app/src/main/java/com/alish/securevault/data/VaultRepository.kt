package com.alish.securevault.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.alish.securevault.model.OpenVaultAsset
import com.alish.securevault.model.RecentVaultItem
import com.alish.securevault.model.SecurityPreference
import com.alish.securevault.model.VaultCategory
import com.alish.securevault.model.VaultItem
import com.alish.securevault.model.VaultStat
import com.alish.securevault.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class VaultRepository(
    private val context: Context,
    private val vaultDao: VaultDao,
    private val cryptoManager: CryptoManager
) {
    private val vaultRoot = File(context.filesDir, "secure_vault").apply { mkdirs() }
    private val encryptedRoot = File(vaultRoot, "content").apply { mkdirs() }
    private val cacheRoot = File(context.cacheDir, "secure_vault/open").apply { mkdirs() }
    
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val statAccents = mapOf(
        VaultCategory.Photos to Color(0xFF4D7CFE),
        VaultCategory.Videos to Color(0xFF00A896),
        VaultCategory.Files to Color(0xFFFF7B54),
        VaultCategory.Audio to Color(0xFFB56BFF),
        VaultCategory.Passwords to Color(0xFFFFC145)
    )
    private val statLabels = mapOf(
        VaultCategory.Photos to "Encrypted memories",
        VaultCategory.Videos to "Private motion",
        VaultCategory.Files to "Sensitive docs",
        VaultCategory.Audio to "Voice notes",
        VaultCategory.Passwords to "Critical access"
    )

    fun getItemsFlow(): Flow<List<VaultItem>> = vaultDao.getAllItems().map { entities ->
        entities.map { entity ->
            VaultItem(
                id = entity.id,
                displayName = entity.displayName,
                category = VaultCategory.valueOf(entity.category),
                mimeType = entity.mimeType,
                sizeBytes = entity.sizeBytes,
                encryptedFileName = entity.encryptedFileName,
                importedAt = entity.importedAt
            )
        }
    }

    suspend fun importFromUris(uris: List<Uri>) = withContext(Dispatchers.IO) {
        uris.forEach { uri ->
            val descriptor = resolveDescriptor(uri) ?: return@forEach
            val itemId = UUID.randomUUID().toString()
            val encryptedFileName = "$itemId.vault"
            val encryptedFile = File(encryptedRoot, encryptedFileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                cryptoManager.encryptToFile(inputStream, encryptedFile)
            } ?: return@forEach

            val entity = VaultItemEntity(
                id = itemId,
                displayName = descriptor.displayName,
                category = descriptor.category.name,
                mimeType = descriptor.mimeType,
                sizeBytes = descriptor.sizeBytes,
                encryptedFileName = encryptedFileName,
                importedAt = formatter.format(Instant.now().atZone(ZoneId.systemDefault()))
            )
            vaultDao.insertItem(entity)
        }
    }

    suspend fun deleteItem(id: String) = withContext(Dispatchers.IO) {
        val items = vaultDao.getAllItems().first()
        val item = items.find { it.id == id } ?: return@withContext
        val encryptedFile = File(encryptedRoot, item.encryptedFileName)
        if (encryptedFile.exists()) {
            encryptedFile.delete()
        }
        vaultDao.deleteItem(id)
    }

    suspend fun prepareOpenAsset(itemId: String): OpenVaultAsset? = withContext(Dispatchers.IO) {
        val items = vaultDao.getAllItems().first()
        val item = items.firstOrNull { it.id == itemId } ?: return@withContext null
        val encryptedFile = File(encryptedRoot, item.encryptedFileName)
        if (!encryptedFile.exists()) return@withContext null

        val outputFile = File(cacheRoot, "${item.id}-${sanitizeFileName(item.displayName)}")
        cryptoManager.decryptToFile(encryptedFile, outputFile)

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        OpenVaultAsset(
            fileName = item.displayName,
            mimeType = item.mimeType,
            contentUri = contentUri.toString()
        )
    }

    suspend fun vaultStats(passwordCount: Int): List<VaultStat> = withContext(Dispatchers.IO) {
        val items = vaultDao.getAllItems().first()
        val itemCounts = items.groupingBy { it.category }.eachCount()
        VaultCategory.entries.map { category ->
            val count = if (category == VaultCategory.Passwords) {
                passwordCount
            } else {
                itemCounts[category.name] ?: 0
            }
            VaultStat(
                category = category,
                itemCount = count,
                label = statLabels.getValue(category),
                accent = statAccents.getValue(category)
            )
        }
    }

    suspend fun recentItems(passwordCount: Int): List<RecentVaultItem> = withContext(Dispatchers.IO) {
        val items = vaultDao.getAllItems().first()
        val importedItems = items.take(5).map {
            RecentVaultItem(
                id = it.id,
                title = it.displayName,
                category = VaultCategory.valueOf(it.category),
                meta = "${formatSize(it.sizeBytes)} - ${it.importedAt}"
            )
        }.toMutableList()

        if (passwordCount > 0) {
            importedItems.add(
                0,
                RecentVaultItem(
                    id = "password-vault",
                    title = "Saved passwords",
                    category = VaultCategory.Passwords,
                    meta = "$passwordCount encrypted entries"
                )
            )
        }
        importedItems
    }

    fun securityPreferences(): List<SecurityPreference> = listOf(
        SecurityPreference("Biometric unlock", "Use fingerprint or face unlock", true),
        SecurityPreference("Device credential fallback", "Allow screen lock PIN fallback", true),
        SecurityPreference("Hide previews", "Encrypted files stay outside shared storage", true),
        SecurityPreference("Auto-lock", "Relock after inactivity", true),
        SecurityPreference("Tamper alerts", "Warn after repeated failed unlock attempts", false)
    )

    private fun resolveDescriptor(uri: Uri): ImportDescriptor? {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val category = categoryForMime(mimeType)
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )

        var displayName = "vault-item-${System.currentTimeMillis()}"
        var sizeBytes = 0L
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) displayName = it.getString(nameIndex)
                if (sizeIndex >= 0) sizeBytes = it.getLong(sizeIndex)
            }
        }

        return ImportDescriptor(
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            category = category
        )
    }

    private fun categoryForMime(mimeType: String): VaultCategory = when {
        mimeType.startsWith("image/") -> VaultCategory.Photos
        mimeType.startsWith("video/") -> VaultCategory.Videos
        mimeType.startsWith("audio/") -> VaultCategory.Audio
        else -> VaultCategory.Files
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
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

    private data class ImportDescriptor(
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long,
        val category: VaultCategory
    )
}
