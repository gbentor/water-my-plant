package com.example.watermyplant.di

import android.content.Context
import com.example.watermyplant.data.TokenManager
import com.example.watermyplant.network.ApiService
import com.example.watermyplant.network.AuthInterceptor
import com.example.watermyplant.network.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideRetrofitInstance(
        authInterceptor: AuthInterceptor
    ): RetrofitInstance {
        return RetrofitInstance(authInterceptor)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofitInstance: RetrofitInstance): ApiService {
        return retrofitInstance.apiService
    }
} 