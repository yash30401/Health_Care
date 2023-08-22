package com.healthcare.yash.preeti.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
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

    // Retrieve arguments using Safe Args
    private val args: DoctorDetailedViewArgs by navArgs()
    private lateinit var reviewsAndRatingsAdapter: ReviewsAndRatingsAdapter
    private lateinit var servicesChipAdatpter: ServicesChipAdatpter

    // GoogleMap instance
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

        // Initialize Google Maps fragment
        val mapFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            this.state = BottomSheetBehavior.STATE_DRAGGING
        }


        // Set up header view
        setupHeaderView()
        // Set up bottom sheet content
        setupBottomSheet()
    }

    // Method to set up the header view
    private fun setupHeaderView() {
        // Load doctor's profile picture using Glide
        Glide.with(this).load(args.doctor.Profile_Pic.toUri()).into(binding.ivDoctorImage)
        binding.tvDoctorNameInDetailView.text = args.doctor.Name
        binding.tvSpecializationDetailedView.text = args.doctor.Specialization

    }

    // Method to set up the bottom sheet content
    private fun setupBottomSheet() {
        // Set up expandable text view for doctor's about information
        binding.tvAboutDoctorDetailedView.setResizableText(args.doctor.About, 4, true)

        // Calculate and display average rating
        val averageRatingTextView = binding.ratingCard.tvDoctorRating
        val averageRating =
            args.doctor.Reviews_And_Ratings.averageRating() // Extension function for calculating the average rating of a list of ReviewsAndRatings
        val formattedRating =
            String.format("%.1f", averageRating)
        averageRatingTextView.text = formattedRating

        // Display number of ratings
        binding.tvRatingNumber.text = "(${args.doctor.Reviews_And_Ratings.size.toString()})"

        // Set up RecyclerView for reviews and ratings
        setupRatingsRecylerView()
        // Set up RecyclerView for services chips
        setupServicesChipView()

        // Set other doctor's information
        binding.tvWorkingHours.text = args.doctor.Working_Hours
        binding.tvCity.text = args.doctor.City
        binding.tvAddress.text = args.doctor.Address
        binding.tvConsultationFee.text = "₹" + args.doctor.Consultation_Fee.toString()
    }

    // Method to set up the RecyclerView for reviews and ratings
    private fun setupRatingsRecylerView() {
        reviewsAndRatingsAdapter = ReviewsAndRatingsAdapter()
        reviewsAndRatingsAdapter.setData(args.doctor.Reviews_And_Ratings)
        binding.reviewRecylerView.apply {
            adapter = reviewsAndRatingsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    // Method to set up the RecyclerView for services chips
    private fun setupServicesChipView() {
        servicesChipAdatpter = ServicesChipAdatpter()
        servicesChipAdatpter.setData(args.doctor.Services)
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

    // GoogleMap's onMapReady callback
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

}