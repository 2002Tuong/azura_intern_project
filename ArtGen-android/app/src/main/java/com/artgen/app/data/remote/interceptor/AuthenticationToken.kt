package com.artgen.app.data.remote.interceptor

data class AuthenticationToken(
    val token: String,
    val expiredAt: Long,
)
