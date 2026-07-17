package com.example.data

import android.util.Base64

object CryptoHelper {
    private const val KEY_CHAR = 'X' // Obfuscation salt

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val xor = plainText.map { (it.code xor KEY_CHAR.code).toChar() }.joinToString("")
        return Base64.encodeToString(xor.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            val decodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
            val decodedStr = String(decodedBytes, Charsets.UTF_8)
            decodedStr.map { (it.code xor KEY_CHAR.code).toChar() }.joinToString("")
        } catch (e: Exception) {
            cipherText
        }
    }
}
