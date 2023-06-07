package com.healthcare.yash.preeti.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.ActivityAuthenticationBinding

class Authentication : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}