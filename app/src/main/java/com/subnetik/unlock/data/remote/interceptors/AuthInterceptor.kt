package com.subnetik.unlock.data.remote.interceptors

import com.subnetik.unlock.data.local.datastore.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authDataStore.accessToken.first() }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept", "application/json")
        }.build()
        val response = chain.proceed(request)
        if (response.code != 200) {
            android.util.Log.w("AuthInterceptor", "${request.method} ${request.url} -> ${response.code} (hasToken=${!token.isNullOrEmpty()})")
        }
        return response
    }
}
