package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "passwords")
data class PasswordCredential(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val username: String,
    val passwordCipher: String,
    val url: String,
    val category: String, // e.g. "Personal", "Work", "Finance", "Social", "Other"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "secure_notes")
data class SecureNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val contentCipher: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true,
    val lockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_configs")
data class SecurityConfig(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val threatsFound: Int,
    val detailsJson: String
)

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAllPasswords(): Flow<List<PasswordCredential>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordCredential)

    @Delete
    suspend fun deletePassword(password: PasswordCredential)
}

@Dao
interface SecureNoteDao {
    @Query("SELECT * FROM secure_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<SecureNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SecureNote)

    @Delete
    suspend fun deleteNote(note: SecureNote)
}

@Dao
interface SecurityConfigDao {
    @Query("SELECT value FROM security_configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: SecurityConfig)
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getScanHistory(): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistory)
}

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockedApp(app: LockedApp)

    @Delete
    suspend fun deleteLockedApp(app: LockedApp)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

@Database(
    entities = [PasswordCredential::class, SecureNote::class, SecurityConfig::class, ScanHistory::class, LockedApp::class],
    version = 2,
    exportSchema = false
)
abstract class SecurityDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    abstract fun secureNoteDao(): SecureNoteDao
    abstract fun securityConfigDao(): SecurityConfigDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun lockedAppDao(): LockedAppDao
}
