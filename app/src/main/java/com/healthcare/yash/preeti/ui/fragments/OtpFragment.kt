package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentOtpBinding
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.TAG
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import `in`.aabhasjindal.otptextview.OTPListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val args: OtpFragmentArgs by navArgs()

    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpBinding.bind(view)

        onClicks()
        setupPhoneNumberTextView()
    }



    private fun onClicks() {

        val verificationID = args.verificationId

        binding.btnVerifyOtp.setOnClickListener {
            val credentitals =
                PhoneAuthProvider.getCredential(
                    verificationID,
                    binding.etOtpPin.editableText.toString()
                )
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                signinWithPhoneNumber(credentitals)
            }
        }

    }

    private fun setupPhoneNumberTextView() {
        val phoneNumber = args.phoneNumber
        val hiddenPhoneNumberText = "+91${phoneNumber.get(3)}${phoneNumber.get(4)}******${phoneNumber.get(11)}${phoneNumber.get(12)}"

        binding.tvPhoneNo.text = hiddenPhoneNumberText
    }

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