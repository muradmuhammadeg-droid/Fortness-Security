package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityRepository(context: Context) {
    private val db: SecurityDatabase = Room.databaseBuilder(
        context.applicationContext,
        SecurityDatabase::class.java,
        "sentinel_security.db"
    ).fallbackToDestructiveMigration().build()

    private val passwordDao = db.passwordDao()
    private val secureNoteDao = db.secureNoteDao()
    private val configDao = db.securityConfigDao()
    private val scanDao = db.scanHistoryDao()
    private val lockedAppDao = db.lockedAppDao()

    // Passwords
    val allPasswords: Flow<List<PasswordCredential>> = passwordDao.getAllPasswords()

    suspend fun insertPassword(credential: PasswordCredential) = withContext(Dispatchers.IO) {
        passwordDao.insertPassword(credential)
    }

    suspend fun deletePassword(credential: PasswordCredential) = withContext(Dispatchers.IO) {
        passwordDao.deletePassword(credential)
    }

    // Secure Notes
    val allNotes: Flow<List<SecureNote>> = secureNoteDao.getAllNotes()

    suspend fun insertNote(note: SecureNote) = withContext(Dispatchers.IO) {
        secureNoteDao.insertNote(note)
    }

    suspend fun deleteNote(note: SecureNote) = withContext(Dispatchers.IO) {
        secureNoteDao.deleteNote(note)
    }

    // Locked Apps
    val allLockedApps: Flow<List<LockedApp>> = lockedAppDao.getAllLockedApps()

    suspend fun insertLockedApp(app: LockedApp) = withContext(Dispatchers.IO) {
        lockedAppDao.insertLockedApp(app)
    }

    suspend fun deleteLockedApp(app: LockedApp) = withContext(Dispatchers.IO) {
        lockedAppDao.deleteLockedApp(app)
    }

    suspend fun deleteLockedAppByPackageName(packageName: String) = withContext(Dispatchers.IO) {
        lockedAppDao.deleteByPackageName(packageName)
    }

    // Security Configs
    suspend fun getMasterPin(): String? = withContext(Dispatchers.IO) {
        configDao.getConfigValue("master_pin")
    }

    suspend fun setMasterPin(pin: String) = withContext(Dispatchers.IO) {
        configDao.setConfig(SecurityConfig("master_pin", pin))
    }

    suspend fun getConfigValue(key: String): String? = withContext(Dispatchers.IO) {
        configDao.getConfigValue(key)
    }

    suspend fun setConfigValue(key: String, value: String) = withContext(Dispatchers.IO) {
        configDao.setConfig(SecurityConfig(key, value))
    }

    suspend fun getLastScanTime(): Long? = withContext(Dispatchers.IO) {
        configDao.getConfigValue("last_scan_time")?.toLongOrNull()
    }

    suspend fun setLastScanTime(time: Long) = withContext(Dispatchers.IO) {
        configDao.setConfig(SecurityConfig("last_scan_time", time.toString()))
    }

    // Scan History
    val scanHistory: Flow<List<ScanHistory>> = scanDao.getScanHistory()

    suspend fun insertScan(scan: ScanHistory) = withContext(Dispatchers.IO) {
        scanDao.insertScan(scan)
    }
}
