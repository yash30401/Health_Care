package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentOtpBinding
import `in`.aabhasjindal.otptextview.OTPListener


class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val args:OtpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpBinding.bind(view)
        val verificationID = args.verificationId

        binding.otpView.otpListener = object : OTPListener{
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}