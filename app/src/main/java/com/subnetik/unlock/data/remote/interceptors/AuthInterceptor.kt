package com.subnetik.unlock.data.remote.interceptors

import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.remote.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val sessionManager: SessionManager,
) : Interceptor {

    companion object {
        /** Paths that should NOT trigger a forced logout on 401. */
        private val AUTH_PATHS = setOf("auth/login", "auth/register", "auth/verify-code")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authDataStore.accessToken.first() }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept", "application/json")
        }.build()

        val response = chain.proceed(request)

        if (response.code != 200 && BuildConfig.DEBUG) {
            android.util.Log.w("AuthInterceptor", "${request.method} ${request.url} -> ${response.code} (hasToken=${!token.isNullOrEmpty()})")
        }

        // Handle expired / invalid token
        if (response.code == 401 && !token.isNullOrEmpty()) {
            val path = request.url.encodedPath.trimStart('/')
            val isAuthEndpoint = AUTH_PATHS.any { path.contains(it) }
            if (!isAuthEndpoint) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("AuthInterceptor", "Session expired – clearing local session")
                }
                runBlocking { authDataStore.clearAll() }
                sessionManager.notifySessionExpired()
            }
        }

        return response
    }
}
