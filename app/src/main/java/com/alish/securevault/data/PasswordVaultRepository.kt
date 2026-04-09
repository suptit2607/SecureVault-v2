package com.alish.securevault.data

import com.alish.securevault.model.PasswordEntry
import com.alish.securevault.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class PasswordVaultRepository(
    private val passwordDao: PasswordDao,
    private val cryptoManager: CryptoManager
) {
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun getEntriesFlow(): Flow<List<PasswordEntry>> = passwordDao.getAllPasswords().map { entities ->
        entities.map { entity ->
            PasswordEntry(
                id = entity.id,
                label = entity.label,
                username = entity.username,
                secret = entity.secret,
                updatedAt = entity.updatedAt
            )
        }
    }

    suspend fun addEntry(label: String, username: String, secret: String) = withContext(Dispatchers.IO) {
        val entry = PasswordEntryEntity(
            id = UUID.randomUUID().toString(),
            label = label,
            username = username,
            secret = cryptoManager.encrypt(secret),
            updatedAt = "Updated ${formatter.format(Instant.now().atZone(ZoneId.systemDefault()))}"
        )
        passwordDao.insertPassword(entry)
    }

    suspend fun deletePassword(id: String) = withContext(Dispatchers.IO) {
        passwordDao.deletePassword(id)
    }

    fun revealSecret(entry: PasswordEntry): String = cryptoManager.decrypt(entry.secret)
}
