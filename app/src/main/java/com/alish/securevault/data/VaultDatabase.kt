package com.alish.securevault.data

import androidx.room.*
import com.alish.securevault.model.VaultCategory
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val category: String,
    val mimeType: String,
    val sizeBytes: Long,
    val encryptedFileName: String,
    val importedAt: String
)

@Entity(tableName = "password_entries")
data class PasswordEntryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val username: String,
    val secret: String, // Encrypted payload
    val updatedAt: String
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY importedAt DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItem(id: String)
}

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password_entries ORDER BY updatedAt DESC")
    fun getAllPasswords(): Flow<List<PasswordEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(entry: PasswordEntryEntity)

    @Query("DELETE FROM password_entries WHERE id = :id")
    suspend fun deletePassword(id: String)
}

@Database(entities = [VaultItemEntity::class, PasswordEntryEntity::class], version = 1)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    abstract fun passwordDao(): PasswordDao
}
