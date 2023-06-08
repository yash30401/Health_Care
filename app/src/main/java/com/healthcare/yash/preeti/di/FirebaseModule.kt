package com.healthcare.yash.preeti.di

import com.google.firebase.auth.FirebaseAuth
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