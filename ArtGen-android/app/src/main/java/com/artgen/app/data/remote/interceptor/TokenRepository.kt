package com.artgen.app.data.remote.interceptor

import android.content.Context
import com.artgen.app.BuildConfig
import com.artgen.app.R
import com.artgen.app.log.Logger
import com.lyft.kronos.KronosClock
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.Date

class TokenRepository(private val clock: KronosClock, private val context: Context) {
    fun syncClock() {
        clock.sync()
    }

    fun generateToken(): AuthenticationToken? {
        return try {
            val currentTime = clock.getCurrentNtpTimeMs()
            if (currentTime == null) {
                syncClock()
            }
            val expiredAt =
                (clock.getCurrentNtpTimeMs() ?: System.currentTimeMillis()) + 5 * ONE_MINUTES_IN_MS
            val token = Jwts.builder()
                .setSubject(JWT_SUBJECT)
                .claim(JWT_CLAIM_PLATFORM, JWT_PLATFORM)
                .claim(JWT_CLAIM_VERSION, BuildConfig.VERSION_NAME)
                .setExpiration(Date(expiredAt))
                .signWith(SignatureAlgorithm.HS256, context.getString(R.string.api_key))
                .compact()
            AuthenticationToken(token, expiredAt)
        } catch (exception: Exception) {
            Logger.e(exception)
            null
        }
    }

    companion object {
        private const val JWT_SUBJECT = "ignore"
        private const val JWT_CLAIM_PLATFORM = "platform"
        private const val JWT_CLAIM_VERSION = "version"
        private const val JWT_PLATFORM = "android"
        private const val ONE_MINUTES_IN_MS = 1 * 60 * 1000L
    }
}
