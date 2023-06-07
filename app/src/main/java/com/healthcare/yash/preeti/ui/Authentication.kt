package com.healthcare.yash.preeti.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityAuthenticationBinding
import com.healthcare.yash.preeti.viewmodels.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Authentication : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!
    private val viewModel:AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            sendVerificationCodeToPhoneNumber()
        }

    }

    private fun sendVerificationCodeToPhoneNumber() {
        val phoneNumber = binding.etPhoneNo.editText?.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.sendVerificationCodeToPhoneNumber(phoneNumber)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}