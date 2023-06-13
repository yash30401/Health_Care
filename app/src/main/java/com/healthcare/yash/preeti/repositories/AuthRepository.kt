package com.healthcare.yash.preeti.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.utils.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {

    //Getting current User
    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    /*
    signinWithPhoneNumber Using Credentials
    * Using Await extension function
    */
    suspend fun signinWithPhoneNo(credential: PhoneAuthCredential): NetworkResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            NetworkResult.Success(result.user!!)
        } catch (e: Exception) {
            NetworkResult.Error(e.message)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}