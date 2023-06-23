package com.healthcare.yash.preeti.di

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class GoogleAuthUiModule {
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext


    @Provides
    fun provideSignInClient(context: Context): SignInClient = Identity.getSignInClient(context)

}