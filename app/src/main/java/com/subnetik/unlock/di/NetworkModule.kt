package com.subnetik.unlock.di

import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.api.AiApi
import com.subnetik.unlock.data.remote.api.AuthApi
import com.subnetik.unlock.data.remote.api.CalendarApi
import com.subnetik.unlock.data.remote.api.BlogApi
import com.subnetik.unlock.data.remote.api.LeadApi
import com.subnetik.unlock.data.remote.api.ReviewsApi
import com.subnetik.unlock.data.remote.api.MarketApi
import com.subnetik.unlock.data.remote.api.NotificationApi
import com.subnetik.unlock.data.remote.api.PaymentApi
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.data.remote.interceptors.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Fallback DNS resolver for debug builds.
     * Android emulators (especially API 36) often have broken system DNS.
     * This tries the system resolver first, then falls back to a hardcoded
     * mapping so the app stays functional during development.
     */
    private val fallbackDns = object : Dns {
        private val hardcoded = mapOf(
            "unlocklingua.com" to listOf(InetAddress.getByName("157.180.90.95")),
        )

        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (e: Exception) {
                hardcoded[hostname]
                    ?: throw e
            }
        }
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .apply { if (BuildConfig.DEBUG) dns(fallbackDns) }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideProgressApi(retrofit: Retrofit): ProgressApi =
        retrofit.create(ProgressApi::class.java)

    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi =
        retrofit.create(AdminApi::class.java)

    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi =
        retrofit.create(AiApi::class.java)

    @Provides
    @Singleton
    fun provideStudentApi(retrofit: Retrofit): StudentApi =
        retrofit.create(StudentApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideMarketApi(retrofit: Retrofit): MarketApi =
        retrofit.create(MarketApi::class.java)

    @Provides
    @Singleton
    fun provideCalendarApi(retrofit: Retrofit): CalendarApi =
        retrofit.create(CalendarApi::class.java)

    @Provides
    @Singleton
    fun provideLeadApi(retrofit: Retrofit): LeadApi =
        retrofit.create(LeadApi::class.java)

    @Provides
    @Singleton
    fun provideReviewsApi(retrofit: Retrofit): ReviewsApi =
        retrofit.create(ReviewsApi::class.java)

    @Provides
    @Singleton
    fun provideBlogApi(retrofit: Retrofit): BlogApi =
        retrofit.create(BlogApi::class.java)
}
