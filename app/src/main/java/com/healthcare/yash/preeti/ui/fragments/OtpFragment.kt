package com.healthcare.yash.preeti.ui.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentOtpBinding
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.COUNTDOWNTIMEINMINUTE
import com.healthcare.yash.preeti.other.Constants.TAG
import com.healthcare.yash.preeti.utils.PhoneAuthCallback
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val args: OtpFragmentArgs by navArgs()

    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var countDownTimer: CountDownTimer
    var isTimerRunning: Boolean? = false
    var currentCounterTimeInMilliSeconds = 0L

    private lateinit var phoneNumber: String

    @Inject
    lateinit var phoneAuthCallback: PhoneAuthCallback

    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpBinding.bind(view)

        callbacks = phoneAuthCallback.callbacks

        onClicks()
        setupPhoneNumberTextView()
        startOtpResendTimer()

        binding.tvPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.ivEditPhoneNo.setOnClickListener {
            editPhoneNumberAndNavigateBackToAuthScreen()
        }

        binding.tvResend.setOnClickListener {
            resendOtpToPhoneNumber()
        }
    }


    private fun onClicks() {

        val verificationID = args.verificationId

        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtpPin.editableText.toString()
            if (otp != "") {
                val credentitals =
                    PhoneAuthProvider.getCredential(
                        verificationID,
                        otp
                    )
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    signinWithPhoneNumber(credentitals)
                }
            }else{
                Toast.makeText(requireContext(), "Please Enter Otp!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //Setting Up Phone Number Textview With Stars(*)
    private fun setupPhoneNumberTextView() {
        phoneNumber = args.phoneNumber
        val hiddenPhoneNumberText =
            "+91${phoneNumber.get(3)}${phoneNumber.get(4)}******${phoneNumber.get(11)}${
                phoneNumber.get(12)
            }"

        binding.tvPhoneNo.text = hiddenPhoneNumberText
    }

    private fun editPhoneNumberAndNavigateBackToAuthScreen() {
        findNavController().navigate(R.id.action_otpFragment_to_authentication2)
    }

    // Timer For OTP Resend
    private fun startOtpResendTimer() {
        currentCounterTimeInMilliSeconds = COUNTDOWNTIMEINMINUTE.toLong() * 60000L
        countDownTimer = object : CountDownTimer(currentCounterTimeInMilliSeconds, 1000) {
            override fun onTick(p0: Long) {
                currentCounterTimeInMilliSeconds = p0
                updateTimerUi()
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE

                binding.tvResend.text = "Resend OTP"
                binding.tvResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blueResendTextColor
                    )
                )
                isTimerRunning = false
            }
        }
        countDownTimer.start()

        isTimerRunning = true
    }

    // Updating the Timer Textview
    private fun updateTimerUi() {
        val minute = (currentCounterTimeInMilliSeconds / 1000) / 60
        val seconds = (currentCounterTimeInMilliSeconds / 1000) % 60

        binding.tvTimer.text = "$minute:$seconds"
    }

    //Resending OTP
    private fun resendOtpToPhoneNumber() {
        if (isTimerRunning == true) {
            Log.d(TAG, "Timer is Running")
        } else {

            val resendToken = args.resendToken.resendingToken

            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken!!)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

            binding.tvResend.text = "Resend OTP in: "
            binding.tvResend.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.tvTimer.visibility = View.VISIBLE
            startOtpResendTimer()
        }
    }


    //SignInWith Credentials
    private suspend fun signinWithPhoneNumber(credentitals: PhoneAuthCredential) {
        //Signin with phone Number
        viewModel?.signinWithPhoneNumber(credentitals)
        delay(3000)

        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
        }

        //And then getting the NetworkReuslt
        viewModel?.loginFlow?.value.let {
            when (it) {
                is NetworkResult.Success -> {
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_otpFragment_to_mainFragment)
                        countDownTimer.cancel()
                    }
                }

                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResult.Loading -> {

                }

                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it?.message.toString(), Toast.LENGTH_SHORT).show()
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