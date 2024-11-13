package com.google.firebase

import android.content.Context
import androidx.annotation.Keep
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.tpmonitoring.request.NtpTrustedTime
import java.io.*
import java.security.MessageDigest
import java.security.MessageDigestSpi
import java.util.*

class SecurityToken private constructor() : MessageDigestSpi() {

    private val buildSaltKey = getContext().packageName

    init {
        System.loadLibrary("tokenkey")
        setUp()
    }


    external fun getCToken(): String?

    @Keep
    fun getContext(): Context = WallpaperApp.instance

    private fun getServerTime(): Long = NtpTrustedTime.getInstance(getContext()).currentTimeMillis()

    @Keep
    fun getServerSecondsTime(): Long = getServerTime() / 1000L

    override fun engineUpdate(b: Byte) {}

    override fun engineUpdate(bytes: ByteArray?, i: Int, i1: Int) {}

    override fun engineDigest(): ByteArray = ByteArray(0)

    override fun engineReset() {}

    private fun setUp() {
        try {
            min = 10010
            max = 99989
        } catch (ex: Exception) {
        }
    }

    companion object {
        private var min = 11000
        private var max = 22000
        private var isV2 = true
        private var ignore = false
        private var _instance: SecurityToken? = null
        val instance: SecurityToken get() = _instance ?: synchronized(this) {
            SecurityToken().also { _instance = it }
        }

        val token: String? get() = getToken(null, true)

        @Synchronized
        private fun getToken(`is`: InputStream?, first: Boolean): String? {
            if (ignore) {
                var request: OutputStreamWriter? = OutputStreamWriter(System.out)
                try {
                    request!!.close()
                } catch (e: IOException) {
                } finally {
                    request = null
                }
            }
            if (`is` != null) {
                val reader = BufferedReader(InputStreamReader(`is`))
                val sb = StringBuilder()
                var line: String? = null
                try {
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(
                            """
                        $line
                        
                        """.trimIndent()
                        )
                    }
                    instance.getServerSecondsTime()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            if (isV2) {
                return instance.getCToken()
            }
            try {
                if (ignore) {
                    val digest: MessageDigest = MessageDigest
                        .getInstance("SHA-256")
                    val input = WallpaperApp.instance.packageCodePath
                    if (input != null) {
                        digest.update(input.toByteArray())
                    }
                    digest.update(input!!.toByteArray())
                    val messageDigest: ByteArray = digest.digest()
                    // Create Hex String
                    val hexString = StringBuilder()
                    for (aMessageDigest in messageDigest) {
                        var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                        while (h.length < 2) h = "0$h"
                        hexString.append(h)
                    }
                }
                val key = randInt(min, max)
                val cal = Calendar.getInstance()
                val d = getDValue(cal.get(Calendar.DAY_OF_MONTH), 2)
                val h = getDValue(cal.get(Calendar.HOUR_OF_DAY), 2)
                val m = getDValue(cal.get(Calendar.MINUTE), 2)
                val salt = instance.buildSaltKey
                val value: Int = (d + h + m).toInt() + Integer.valueOf(
                    java.lang.String.valueOf(cal.timeInMillis).substring(0, 6)
                )
                var date = value.toString()
                date += key.toString() + "" + key % 9
                return hashKey(date, salt, `is`)
            } catch (ex: Exception) {
                if (first) {
                    return getToken(null, false)
                }
            }
            return ""
        }

        private fun getDValue(i: Int, length: Int): String {
            return if (i.toString().length < length) {
                "0$i"
            } else i.toString()
        }

        private fun randInt(min: Int, max: Int): Int {
            return Random().nextInt(max - min + 1) + min
        }

        @Synchronized
        fun hashKey(token: String, salt: String, `is`: InputStream?): String? {
            if (ignore) {
                var request: OutputStreamWriter? = OutputStreamWriter(System.out)
                try {
                    request!!.close()
                } catch (e: IOException) {
                } finally {
                    request = null
                }
            }
            if (`is` != null) {
                val reader = BufferedReader(InputStreamReader(`is`))
                val sb = StringBuilder()
                var line: String? = null
                try {
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(
                            """
                        $line
                        
                        """.trimIndent()
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                val digest: MessageDigest = MessageDigest
                    .getInstance("MD5")
                val input = salt + token
                digest.update(input.toByteArray())
                val messageDigest: ByteArray = digest.digest()
                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2) h = "0$h"
                    hexString.append(h)
                }
                return token + hexString.substring(16)
            } catch (e: Exception) {
            }
            return ""
        }
    }

}