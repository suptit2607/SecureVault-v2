package com.alish.securevault.data

import android.content.Context
import com.alish.securevault.model.PasswordEntry
import com.alish.securevault.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class PasswordVaultRepository(
    context: Context,
    private val cryptoManager: CryptoManager
) {
    private val vaultRoot = File(context.filesDir, "secure_vault").apply { mkdirs() }
    private val metadataFile = File(vaultRoot, "passwords.json")
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())

    suspend fun getEntries(): List<PasswordEntry> = withContext(Dispatchers.IO) {
        readEntries()
    }

    suspend fun addEntry(label: String, username: String, secret: String): List<PasswordEntry> =
        withContext(Dispatchers.IO) {
            val current = readEntries().toMutableList()
            current.add(
                0,
                PasswordEntry(
                    id = UUID.randomUUID().toString(),
                    label = label,
                    username = username,
                    secret = cryptoManager.encrypt(secret),
                    updatedAt = "Updated ${formatter.format(Instant.now().atZone(ZoneId.systemDefault()))}"
                )
            )
            writeEntries(current)
            current
        }

    fun revealSecret(entry: PasswordEntry): String = cryptoManager.decrypt(entry.secret)

    private fun readEntries(): List<PasswordEntry> {
        if (!metadataFile.exists()) return emptyList()
        val payload = metadataFile.readText()
        if (payload.isBlank()) return emptyList()

        val array = JSONArray(payload)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    PasswordEntry(
                        id = item.getString("id"),
                        label = item.getString("label"),
                        username = item.getString("username"),
                        secret = item.getString("secret"),
                        updatedAt = item.getString("updatedAt")
                    )
                )
            }
        }
    }

    private fun writeEntries(entries: List<PasswordEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("label", entry.label)
                    .put("username", entry.username)
                    .put("secret", entry.secret)
                    .put("updatedAt", entry.updatedAt)
            )
        }
        metadataFile.writeText(array.toString())
    }
}
