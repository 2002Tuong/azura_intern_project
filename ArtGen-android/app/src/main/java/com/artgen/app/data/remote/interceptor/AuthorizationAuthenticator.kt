package com.artgen.app.data.remote.interceptor

import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.interceptor.AuthorizationInterceptor.Companion.BEARER
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthorizationAuthenticator(
    private val tokenRepository: TokenRepository,
    private val appDataStore: AppDataStore
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            tokenRepository.syncClock()
            when (val token = tokenRepository.generateToken()) {
                null -> null
                else -> {
                    appDataStore.authorization = token.token
                    appDataStore.authorizationExpiredAt = token.expiredAt
                    response.request.newBuilder()
                        .removeHeader(AuthorizationInterceptor.AUTHORIZATION)
                        .addHeader(AuthorizationInterceptor.AUTHORIZATION, "$BEARER ${token.token}")
                        .build()
                }
            }
        }
    }
}
