package com.artgen.app.data.remote.interceptor

import android.content.Context
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import java.util.Calendar
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
    private val tokenRepository: TokenRepository,
    private val context: Context,
    private val dataStore: AppDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = chain.request().newBuilder()
        if (request.headers[AUTHORIZATION].isNullOrEmpty() &&
            request.url.toUrl().toString().contains(context.getString(R.string.base_endpoint))
        ) {
            if (willExpireWithinOneMinute(dataStore.authorizationExpiredAt)) {
                val token = tokenRepository.generateToken() ?: return chain.proceed(request)
                builder.addHeader(AUTHORIZATION, "$BEARER ${token.token}")
                dataStore.authorization = token.token
                dataStore.authorizationExpiredAt = token.expiredAt
            } else {
                builder.addHeader(AUTHORIZATION, "$BEARER ${dataStore.authorization}")
            }
        }
        return chain.proceed(builder.build())
    }

    private fun willExpireWithinOneMinute(expiredAt: Long) =
        expiredAt - Calendar.getInstance().timeInMillis < 60_000

    companion object {
        internal const val AUTHORIZATION = "Authorization"
        internal const val BEARER = "Bearer"
    }
}
