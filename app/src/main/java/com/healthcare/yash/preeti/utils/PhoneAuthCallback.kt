package com.healthcare.yash.preeti.utils

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.PhoneAuthCallbackSealedClass

import com.healthcare.yash.preeti.ui.fragments.AuthenticationDirections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PhoneAuthCallback @Inject constructor() {
    private val _callBackFlow: MutableStateFlow<PhoneAuthCallbackSealedClass?> =
        MutableStateFlow(null)
    val callBackFlow: StateFlow<PhoneAuthCallbackSealedClass?> = _callBackFlow



    val callbacks = object:PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            _callBackFlow.value = PhoneAuthCallbackSealedClass.ONVERIFICATIONCOMPLETED()
        }

        override fun onVerificationFailed(e: FirebaseException) {

//                Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                binding.progressBar.visibility = View.GONE
            _callBackFlow.value = PhoneAuthCallbackSealedClass.ONVERIFICATIONFAILED(e.message)

            if (e is FirebaseAuthInvalidCredentialsException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
                _callBackFlow.value =
                    PhoneAuthCallbackSealedClass.FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION(e.message)
            } else if (e is FirebaseTooManyRequestsException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                _callBackFlow.value =
                    PhoneAuthCallbackSealedClass.FIREBASETOOMANYREQUESTSEXCEPTION(e.message)
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
//                    Log.w(Constants.AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                _callBackFlow.value =
                    PhoneAuthCallbackSealedClass.FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION(e.message)
            }

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
//                Log.d("AUTHVERIFICATION", "onCodeSent:$verificationId")
//                storedVerificationId = verificationId
//                resendToken = token
//
//                val action =
//                    AuthenticationDirections.actionAuthentication2ToOtpFragment(
//                        verificationId,
//                        phoneNumber
//                    )
//                findNavController().navigate(action)
            Log.d(Constants.AUTHVERIFICATIONTAG,"codeSent")
            _callBackFlow.value = PhoneAuthCallbackSealedClass.ONCODESENT(verificationId, token)
        }


    }

}