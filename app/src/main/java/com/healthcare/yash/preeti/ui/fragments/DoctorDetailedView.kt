package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.ReviewsAndRatingsAdapter
import com.healthcare.yash.preeti.adapters.ServicesChipAdatpter
import com.healthcare.yash.preeti.databinding.FragmentDoctorDetailedViewBinding
import com.healthcare.yash.preeti.utils.averageRating
import com.healthcare.yash.preeti.utils.setResizableText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorDetailedView : Fragment(R.layout.fragment_doctor_detailed_view), OnMapReadyCallback {

    private var _binding: FragmentDoctorDetailedViewBinding? = null
    private val binding get() = _binding!!

    private val args: DoctorDetailedViewArgs by navArgs()
    private lateinit var reviewsAndRatingsAdapter: ReviewsAndRatingsAdapter
    private lateinit var servicesChipAdatpter: ServicesChipAdatpter

    private lateinit var map: GoogleMap

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

        val mapFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            this.state = BottomSheetBehavior.STATE_DRAGGING
        }

        setupHeaderView()
        setupBottomSheet()
    }


    private fun setupHeaderView() {
        Glide.with(this).load(args.doctor.Profile_Pic.toUri()).into(binding.ivDoctorImage)
        binding.tvDoctorNameInDetailView.text = args.doctor.Name
        binding.tvSpecializationDetailedView.text = args.doctor.Specialization

    }

    private fun setupBottomSheet() {
        binding.tvAboutDoctorDetailedView.setResizableText(args.doctor.About, 4, true)

        val averageRatingTextView = binding.ratingCard.tvDoctorRating


        val averageRating = args.doctor.Reviews_And_Ratings.averageRating()

        val formattedRating =
            String.format("%.1f", averageRating)
        averageRatingTextView.text = formattedRating

        binding.tvRatingNumber.text = "(${args.doctor.Reviews_And_Ratings.size.toString()})"

        setupRatingsRecylerView()
        setupServicesChipView()

        binding.tvWorkingHours.text = args.doctor.Working_Hours

        binding.tvCity.text = args.doctor.City
        binding.tvAddress.text = args.doctor.Address
        binding.tvConsultationFee.text = "₹"+args.doctor.Consultation_Fee.toString()
    }


    private fun setupRatingsRecylerView() {
        reviewsAndRatingsAdapter = ReviewsAndRatingsAdapter(args.doctor.Reviews_And_Ratings)
        binding.reviewRecylerView.apply {
            adapter = reviewsAndRatingsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupServicesChipView() {
        servicesChipAdatpter = ServicesChipAdatpter(args.doctor.Services)
        binding.chipRecylerView.apply {
            adapter = servicesChipAdatpter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

}