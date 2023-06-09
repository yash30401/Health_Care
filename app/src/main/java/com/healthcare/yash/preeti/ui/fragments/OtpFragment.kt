package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentOtpBinding
import com.healthcare.yash.preeti.networking.NetworkResult
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

        val verificationID = args.verificationId

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                val credentitals = PhoneAuthProvider.getCredential(verificationID, otp)
                binding.otpProgressBar.visibility = View.VISIBLE
                GlobalScope.launch(Dispatchers.IO) {
                    signinWithPhoneNumber(credentitals)

                }

            }

        }
    }

    private suspend fun signinWithPhoneNumber(credentitals: PhoneAuthCredential) {
        viewModel?.signinWithPhoneNumber(credentitals)
        delay(1000)

        withContext(Dispatchers.Main) {
            binding.otpProgressBar.visibility = View.GONE
        }

        viewModel?.loginFlow?.value.let {
            when (it) {
                is NetworkResult.Success -> {
                    findNavController().navigate(R.id.action_otpFragment_to_mainFragment)
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