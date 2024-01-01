package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toolbar
import androidx.core.net.toUri
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentChattingBinding



class ChattingFragment : Fragment(R.layout.fragment_chatting) {

    private var _binding:FragmentChattingBinding?=null
    private val binding get()= _binding!!

    private val args:ChattingFragmentArgs by navArgs<ChattingFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChattingBinding.bind(view)

        val toolbarLayout = binding.toolbar
        val toolbar:Toolbar  = toolbarLayout.toolbar

        activity?.setActionBar(toolbar)
        activity?.actionBar?.setDisplayShowTitleEnabled(false)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

        val ivProfilePicture:ImageView = toolbarLayout.ivProfilePicture
        Glide.with(this).load(args.detailedUserAppointment.profileImage.toUri()).into(ivProfilePicture)

        toolbarLayout.tvDoctoreName.text = args.detailedUserAppointment.name

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}