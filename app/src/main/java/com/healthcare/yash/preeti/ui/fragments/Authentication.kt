package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import com.healthcare.yash.preeti.databinding.FragmentAuthenticationBinding
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Authentication : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_authentication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthenticationBinding.bind(view)


        binding.btnRegister.setOnClickListener {
            val phoneNumber = validatePhoneNumber(binding.etPhoneNo.editText?.text.toString())
            if (phoneNumber == true) {
                sendVerificationCodeToPhoneNumber()
            } else {
                Toast.makeText(context, "Please check your phone number!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }


    // Function to validate Indian phone numbers
    fun validatePhoneNumber(number: String): Boolean {
        val regex = Regex("^\\+91[1-9]\\d{9}$")
        return regex.matches(number)
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

                findNavController().navigate(R.id.action_authentication2_to_otpFragment)
            }
        }

        val phoneNumber = binding.etPhoneNo.editText?.text.toString()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}