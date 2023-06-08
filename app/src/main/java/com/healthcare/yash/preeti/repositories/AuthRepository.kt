package com.healthcare.yash.preeti.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
) {

    init {

    }

//    suspend fun signinWithPhoneNo(): NetworkResult<FirebaseUser> {
//
//    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//
//    }


    fun logout() {

    }
}