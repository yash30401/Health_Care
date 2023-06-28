package com.healthcare.yash.preeti.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentAuthenticationBinding
import com.healthcare.yash.preeti.googleAuth.GoogleAuthUiClient
import com.healthcare.yash.preeti.models.ResendTokenModelClass
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.AUTHVERIFICATIONTAG
import com.healthcare.yash.preeti.other.Constants.FACEBOOKTEST
import com.healthcare.yash.preeti.other.Constants.TAG
import com.healthcare.yash.preeti.other.PhoneAuthCallbackSealedClass
import com.healthcare.yash.preeti.other.PhoneNumberValidation
import com.healthcare.yash.preeti.utils.PhoneAuthCallback
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONException
import java.util.concurrent.TimeUnit
import javax.annotation.meta.When
import javax.inject.Inject

@AndroidEntryPoint
class Authentication : Fragment() {

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback

    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var googleAuthUiClient: GoogleAuthUiClient

    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    @Inject
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (firebaseAuth.currentUser != null) {
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

        callback = phoneAuthCallback.callbacks

        launcher = registerActivityForResult()

        binding.btnRequestOtp.setOnClickListener {
            val phoneNoValidation = validatePhoneNumber(binding.etMobileNo.text.toString())
            phoneNoEventsHandle(phoneNoValidation)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnFacebookSignIn.setOnClickListener {
            signInWithFacebook()
        }
    }

    // Function to validate Phone numbers
    private fun validatePhoneNumber(number: String): PhoneNumberValidation =
        if (number.isEmpty()) PhoneNumberValidation.EMPTY else PhoneNumberValidation.SUCCESS


    // Handling PhoneNumber Input Events
    private fun phoneNoEventsHandle(phoneNoValidation: PhoneNumberValidation) {
        when (phoneNoValidation) {
            PhoneNumberValidation.SUCCESS -> {
                binding.progressBar.visibility = View.VISIBLE
                sendVerificationCodeToPhoneNumber()
            }

            PhoneNumberValidation.EMPTY -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT)
                    .show()
            }

            PhoneNumberValidation.WRONGFORMAT -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // Verifying PhoneNumber And Sending OTP
    private fun sendVerificationCodeToPhoneNumber() {
        val phoneNumber =
            "${binding.etCountryCode.selectedCountryCodeWithPlus}${binding.etMobileNo.text.toString()}"

//        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                Log.d(AUTHVERIFICATIONTAG, "onVerificationCompleted:$credential")
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//
//                Log.w(AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                binding.progressBar.visibility = View.GONE
//
//                if (e is FirebaseAuthInvalidCredentialsException) {
//                    Log.w(AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
//                } else if (e is FirebaseTooManyRequestsException) {
//                    Log.w(AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
//                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
//                    Log.w(AUTHVERIFICATIONTAG, "onVerificationFailed", e)
//                    Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCodeSent(
//                verificationId: String,
//                token: PhoneAuthProvider.ForceResendingToken,
//            ) {
//                Log.d("AUTHVERIFICATION", "onCodeSent:$verificationId")
//                storedVerificationId = verificationId
//                resendToken = token
//
//                val action =
//                    AuthenticationDirections.actionAuthentication2ToOtpFragment(
//                               verificationId,
//                                phoneNumber,
//                                ResendTokenModelClass(resendToken)
//                            )
//                findNavController().navigate(action)
//            }
//        }


        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)
            phoneAuthCallback.callBackFlow?.collect {
                when (it) {
                    is PhoneAuthCallbackSealedClass.ONVERIFICATIONCOMPLETED -> {
                        Log.d(AUTHVERIFICATIONTAG, "Verification Completed")
                    }

                    is PhoneAuthCallbackSealedClass.ONVERIFICATIONFAILED -> {
                        Log.d(AUTHVERIFICATIONTAG, "onVerificationFailed: ${it.firebaseException}")
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                    is PhoneAuthCallbackSealedClass.FIREBASEAUTHINVALIDCREDENTIALSEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthInvalidCredentialsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    is PhoneAuthCallbackSealedClass.FIREBASETOOMANYREQUESTSEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseTooManyRequestsException}"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Too many requests", Toast.LENGTH_SHORT).show()
                        }

                    }

                    is PhoneAuthCallbackSealedClass.FIREBASEAUTHMISSINGACTIVITYFORRECAPTCHAEXCEPTION -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it.firebaseAuthMissingActivityForRecaptchaException}"
                        )
                        withContext(Dispatchers.IO) {
                            Toast.makeText(context, "reCaptcha Problem", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is PhoneAuthCallbackSealedClass.ONCODESENT -> {
                        Log.d("AUTHVERIFICATION", "onCodeSent:${it.verificationId}")
                        storedVerificationId = it.verificationId.toString()
                        resendToken = it.token!!

                        val action =
                            AuthenticationDirections.actionAuthentication2ToOtpFragment(
                                it.verificationId.toString(),
                                phoneNumber,
                                ResendTokenModelClass(resendToken)
                            )
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(action)
                        }
                    }

                    else -> {
                        Log.d(
                            AUTHVERIFICATIONTAG,
                            "onVerificationFailed: ${it?.firebaseException}"
                        )
                    }
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    //Signing With Google Account
    private fun signInWithGoogle() {
        /*Because OneTap Google SignIn sends an Intent. So thats why we used this way to create google SignIn using clean architecture.
        */
        Log.e(TAG, "signInWithGoogle: ")
        lifecycleScope.launch {
            val signInIntentSender = googleAuthUiClient.signIn()
            Log.e(TAG, "signInWithGoogle: $signInIntentSender")
            launcher.launch(
                IntentSenderRequest.Builder(
                    signInIntentSender ?: return@launch
                ).build()
            )

            viewModel.googleSignInState?.collect {
                Log.e(TAG, "signInViewModel: $it")
                when (it) {
                    is NetworkResult.Loading -> {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }

                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Success: ")
                            findNavController().navigate(R.id.action_authentication2_to_mainFragment)
                            Toast.makeText(
                                requireContext(),
                                "Sign In Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    else -> {
                        Log.d(TAG, "An Unknow Error occur")
                    }
                }
            }
        }
    }

    /*
    * ActivityResultLauncher is the new face of the trigger mechanism for Kotlin. This launcher has been used since 2020, moreover,
    * startActivityForResult method died in 2020.
    * */
    private fun registerActivityForResult(): ActivityResultLauncher<IntentSenderRequest> {
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->

                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        val signInResult = googleAuthUiClient.signInWithIntent(
                            intent = result.data ?: return@launch
                        )
                        viewModel.onGoogleSignInResult(signInResult)
                    }
                }

            }
        return launcher
    }


    private fun signInWithFacebook() {
        viewModel.signInWithFacebook(callbackManager, this@Authentication)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.facebookSigninState?.collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        withContext(Dispatchers.Main){
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        Log.d(FACEBOOKTEST, "Loading")
                    }

                    is NetworkResult.Success -> {
                        withContext(Dispatchers.Main) {
                            Log.d(FACEBOOKTEST, "signInWithFacebook: Success")
                            findNavController().navigate(R.id.action_authentication2_to_mainFragment)
                            Toast.makeText(
                                requireContext(),
                                "Sign In Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    else -> {
                        Log.d(TAG, "An Unknow Error occur")
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}