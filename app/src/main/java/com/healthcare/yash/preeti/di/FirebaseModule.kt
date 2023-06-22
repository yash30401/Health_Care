package com.healthcare.yash.preeti.di

import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.googleAuth.GoogleAuthUiClient
import com.healthcare.yash.preeti.repositories.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {

    @Provides
    fun providesFirebaseAuth():FirebaseAuth = FirebaseAuth.getInstance()

}