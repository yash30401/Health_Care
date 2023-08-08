package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.databinding.FragmentDoctorDetailedViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorDetailedView : Fragment(R.layout.fragment_doctor_detailed_view) {

    private var _binding: FragmentDoctorDetailedViewBinding? = null
    private val binding get() = _binding!!

    private val args:DoctorDetailedViewArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_doctor_detailed_view, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentDoctorDetailedViewBinding.bind(view)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            this.state = BottomSheetBehavior.STATE_DRAGGING
        }

        setupHeaderView()
    }

    private fun setupHeaderView() {
        Glide.with(this).load(args.doctor.Profile_Pic.toUri()).into(binding.ivDoctorImage)
        binding.tvDoctorNameInDetailView.text = args.doctor.Name
        binding.tvSpecializationDetailedView.text = args.doctor.Specialization
        binding.tvAboutDoctorDetailedView.text = args.doctor.About

        val averageRatingTextView = binding.ratingCard.tvDoctorRating

        var averageRating: Double = 0.0
        args.doctor.Reviews_And_Ratings.forEach {
            val rating = it.rating.toDouble()
            averageRating += rating
        }
        val formattedRating = String.format("%.1f", averageRating / args.doctor.Reviews_And_Ratings.size)
        averageRatingTextView.text = formattedRating

        binding.tvRatingNumber.text = "(${args.doctor.Reviews_And_Ratings.size.toString()})"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}