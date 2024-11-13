package com.parallax.hdvideo.wallpapers.utils.other

import android.util.Base64
import com.parallax.hdvideo.wallpapers.extension.toHex
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AES {
    private var salt = ""
    var IV = ByteArray(16)
    private val iterationCount = 65536
    private val lengthOfKey = 256

    init {
        var fileOut: FileOutputStream? = null
        var xx = 0

        try {
            xx = lengthOfKey / IV.size
            if (IV.isEmpty()) {
                for (i in 0..xx) {
                    val file = File(IV.contentToString())
                    fileOut = FileOutputStream(file)
                }
            }
        } catch (e: Exception) {
            Logger.e(e, "checkBuild Error: " + e.message)
        } finally {
            try {
                xx = xx shr 1
                for (i in 0 until xx) {
                    IV[i] = (i / 2 ).toByte()
                }
                salt = IV.joinToString(separator = "", limit = xx shr 1).toHex()
                fileOut?.close()
            } catch (e: IOException) {
                Logger.e(e, "checkBuild Close Error: " + e.message)
            }
        }
    }

    private fun getKey(secret: String) : SecretKeySpec? {
        try {
            var secretKey = secret.toByteArray(charset("UTF-8"))
            val shaInstance = MessageDigest.getInstance("SHA-256")
            secretKey = shaInstance.digest(secretKey)
            secretKey = secretKey.copyOf(16)
            return SecretKeySpec(secretKey, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    fun enc(text: String, secret: String): String? {
        try {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val key = getKey(secret) ?: return null
                val cipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipherInstance.init(Cipher.ENCRYPT_MODE, key)
                return Base64.encodeToString(cipherInstance.doFinal(text.toByteArray(charset("UTF-8"))), Base64.DEFAULT)
//            } else {
//                val ivSpec = IvParameterSpec(iv)
//                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//                val spec: KeySpec =
//                    PBEKeySpec(secret.toCharArray(), salt.toByteArray(), iterationCount, keyLength)
//                val tmp: SecretKey = factory.generateSecret(spec)
//                val secretKey = SecretKeySpec(tmp.encoded, "AES")
//                val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
//                return Base64.encodeToString(
//                    cipher.doFinal(text.toByteArray(charset("UTF-8"))),
//                    Base64.DEFAULT
//                )
//            }
        } catch (e: Exception) {
            Logger.d("Error while encrypting: $e")
        }
        return null
    }

    fun dec(text: String, secret: String): String? {
        try {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val key = getKey(secret) ?: return null
                val cipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipherInstance.init(Cipher.DECRYPT_MODE, key)
                return String(cipherInstance.doFinal(Base64.decode(text, Base64.DEFAULT)))
//            } else {
//                val ivSpec = IvParameterSpec(iv)
//                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//                val spec: KeySpec =
//                    PBEKeySpec(secret.toCharArray(), salt.toByteArray(), iterationCount, keyLength)
//                val tmp = factory.generateSecret(spec)
//                val secretKey = SecretKeySpec(tmp.encoded, "AES")
//                val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
//                return String(cipher.doFinal(Base64.decode(text, Base64.DEFAULT)))
//            }
        } catch (e: Exception) {
            Logger.d("Error while decrypting: $e")
        }
        return null
    }
}