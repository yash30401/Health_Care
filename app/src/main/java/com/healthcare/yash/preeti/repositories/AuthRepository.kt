package com.healthcare.yash.preeti.repositories

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.healthcare.yash.preeti.networking.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val activity: Activity
) {

    private var currentUser: FirebaseUser?
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks?
    private lateinit var storeVerificationId:String
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    init {
        currentUser = firebaseAuth.currentUser
        callbacks = null
    }

    suspend fun sendVerificationCodeToPhoneNumber(phoneNo: String){
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNo)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

//        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
////                signInWithPhoneAuthCredential(credential)
//            }
//
//            override fun onVerificationFailed(firebaseException: FirebaseException) {
//                when (firebaseException) {
//                    is FirebaseAuthInvalidCredentialsException -> {
//                        Toast.makeText(context, "Invalid Request", Toast.LENGTH_SHORT).show()
//                    }
//                    is FirebaseTooManyRequestsException->{
//                        Toast.makeText(context, "Too Many Requests", Toast.LENGTH_SHORT).show()
//                    }
//                    is FirebaseAuthMissingActivityForRecaptchaException->{
//                        Toast.makeText(context, "reCAPTCHA verification attempted with null Activity", Toast.LENGTH_SHORT).show()
//                    }
//                    else->{
//                        Toast.makeText(context, "An unknow error occured!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//
//            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                Log.d("AUTHVERIFICATION", "onCodeSent:$verificationId")
//                storeVerificationId = verificationId
//                resendingToken = token
//            }
//        }
    }
//
//    suspend fun signinWithPhoneNo(): NetworkResult<FirebaseUser> {
//
//    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//
//    }


    fun logout() {

    }
}