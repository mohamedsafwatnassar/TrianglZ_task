package com.trianglz.chatapp.di

import android.content.Context
import com.trianglz.chatapp.data.local.UserPreferencesDataSource
import com.trianglz.chatapp.data.remote.FirebaseDataSource
import com.trianglz.chatapp.data.remote.FirebaseStorageDataSource
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data source dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    /**
     * Provides Firebase Realtime Database data source
     */
    @Provides
    @Singleton
    fun provideFirebaseDataSource(
        database: FirebaseDatabase
    ): FirebaseDataSource {
        return FirebaseDataSource(database)
    }

    /**
     * Provides Firebase Storage data source for media uploads
     * Uses Realtime Database instead of Firebase Storage
     */
    @Provides
    @Singleton
    fun provideFirebaseStorageDataSource(
        @ApplicationContext context: Context,
        database: FirebaseDatabase
    ): FirebaseStorageDataSource {
        return FirebaseStorageDataSource(context, database)
    }

    /**
     * Provides DataStore-based user preferences data source
     */
    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        @ApplicationContext context: Context
    ): UserPreferencesDataSource {
        return UserPreferencesDataSource(context)
    }
}