package com.subnetik.unlock.di

import com.subnetik.unlock.data.repository.AiRepositoryImpl
import com.subnetik.unlock.data.repository.AuthRepositoryImpl
import com.subnetik.unlock.data.repository.NotificationRepositoryImpl
import com.subnetik.unlock.data.repository.ProgressRepositoryImpl
import com.subnetik.unlock.data.repository.TestRepositoryImpl
import com.subnetik.unlock.data.repository.VocabularyRepositoryImpl
import com.subnetik.unlock.domain.repository.AiRepository
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.domain.repository.NotificationRepository
import com.subnetik.unlock.domain.repository.ProgressRepository
import com.subnetik.unlock.domain.repository.TestRepository
import com.subnetik.unlock.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindTestRepository(impl: TestRepositoryImpl): TestRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
}
