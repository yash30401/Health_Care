package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentAuthenticationBinding
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Authentication : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(firebaseAuth.currentUser !=null){
            findNavController().navigate(R.id.action_authentication2_to_mainFragment)
        }
    }

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
            binding.progressBar.visibility = View.VISIBLE
            val phoneNumber = validatePhoneNumber(binding.etPhoneNo.editText?.text.toString())
            if (phoneNumber == true) {
                sendVerificationCodeToPhoneNumber()
            } else {
                binding.progressBar.visibility = View.GONE
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
                binding.progressBar.visibility = View.GONE

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                    Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                    Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    Log.w("AUTHVERIFICATION", "onVerificationFailed", e)
                    Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d("AUTHVERIFICATION", "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token

                val action = AuthenticationDirections.actionAuthentication2ToOtpFragment(verificationId)
                findNavController().navigate(action)
            }
        }



        val phoneNumber = binding.etPhoneNo.editText?.text.toString()
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
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