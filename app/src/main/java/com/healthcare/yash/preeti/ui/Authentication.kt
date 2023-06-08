package com.healthcare.yash.preeti.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityAuthenticationBinding
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Authentication : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            sendVerificationCodeToPhoneNumber()
        }

    }

    private fun sendVerificationCodeToPhoneNumber() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("AUTHVERIFICATION", "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.w("AUTHVERIFICATION", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d("AUTHVERIFICATION", "onCodeSent:$verificationId")

                storedVerificationId = verificationId
                resendToken = token
            }
        }

        val phoneNumber = binding.etPhoneNo.editText?.text.toString()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}