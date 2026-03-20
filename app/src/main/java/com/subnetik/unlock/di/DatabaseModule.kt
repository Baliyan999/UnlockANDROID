package com.subnetik.unlock.di

import android.content.Context
import androidx.room.Room
import com.subnetik.unlock.data.local.db.UnlockDatabase
import com.subnetik.unlock.data.local.db.dao.TestProgressDao
import com.subnetik.unlock.data.local.db.dao.VocabularyProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UnlockDatabase {
        return Room.databaseBuilder(
            context,
            UnlockDatabase::class.java,
            "unlock_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTestProgressDao(db: UnlockDatabase): TestProgressDao =
        db.testProgressDao()

    @Provides
    fun provideVocabularyProgressDao(db: UnlockDatabase): VocabularyProgressDao =
        db.vocabularyProgressDao()
}
