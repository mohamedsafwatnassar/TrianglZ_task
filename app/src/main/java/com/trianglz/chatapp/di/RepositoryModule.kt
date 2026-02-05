package com.trianglz.chatapp.di

import com.trianglz.chatapp.data.repositoryImpl.UserRepositoryImpl
import com.trianglz.chatapp.data.local.UserPreferencesDataSource
import com.trianglz.chatapp.data.remote.FirebaseDataSource
import com.trianglz.chatapp.data.repositoryImpl.MessageRepositoryImpl
import com.trianglz.chatapp.domain.repository.MessageRepository
import com.trianglz.chatapp.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides UserRepository implementation
     * Handles user preferences and typing status
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        userPreferencesDataSource: UserPreferencesDataSource,
        firebaseDataSource: FirebaseDataSource
    ): UserRepository {
        return UserRepositoryImpl(userPreferencesDataSource, firebaseDataSource)
        // return UserRepositoryImpl(userPreferencesDataSource)
    }

    /**
     * Provides MessageRepository implementation
     * Binds the implementation to the interface for dependency injection
     */
    @Provides
    @Singleton
    fun provideMessageRepository(
        firebaseDataSource: FirebaseDataSource
    ): MessageRepository {
        return MessageRepositoryImpl(firebaseDataSource)
    }
}