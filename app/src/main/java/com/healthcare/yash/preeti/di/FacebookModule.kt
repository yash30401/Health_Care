package com.healthcare.yash.preeti.di

import com.facebook.CallbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FacebookModule {

    @Provides
    fun providesCallBackManager():CallbackManager  = CallbackManager.Factory.create()
}