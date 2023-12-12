package com.healthcare.yash.preeti.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.AppointmentTimeAdapter
import com.healthcare.yash.preeti.adapters.ReviewsAndRatingsAdapter
import com.healthcare.yash.preeti.adapters.ServicesChipAdatpter
import com.healthcare.yash.preeti.databinding.FragmentDoctorDetailedViewBinding
import com.healthcare.yash.preeti.models.DoctorAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.other.Constants.APPOINTMENTADDED
import com.healthcare.yash.preeti.ui.MainActivity
import com.healthcare.yash.preeti.utils.averageRating
import com.healthcare.yash.preeti.utils.setResizableText
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.healthcare.yash.preeti.viewmodels.SlotViewModel
import com.razorpay.Checkout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class DoctorDetailedView : Fragment(R.layout.fragment_doctor_detailed_view), OnMapReadyCallback {

    private var _binding: FragmentDoctorDetailedViewBinding? = null
    private val binding get() = _binding!!

    // Retrieve arguments using Safe Args
    private var args: DoctorDetailedViewArgs?=null
    private lateinit var reviewsAndRatingsAdapter: ReviewsAndRatingsAdapter
    private lateinit var servicesChipAdatpter: ServicesChipAdatpter


    lateinit var firebaseAuth: FirebaseAuth

    // GoogleMap instance
    private lateinit var map: GoogleMap

    private val slotViewModel by viewModels<SlotViewModel>()

    private val appointmentViewModel by viewModels<AppointmentViewModel>()
    private lateinit var slotAdapter: AppointmentTimeAdapter


    var storeConsultText: String = ""
    var storeTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_doctor_detailed_view, container, false)
        Log.d("LIFECYCLEOFFRAG", "OnCreateView")
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentDoctorDetailedViewBinding.bind(view)

        arguments?.let {
            args = DoctorDetailedViewArgs.fromBundle(it)
        }
        // Initialize Google Maps fragment
        val mapFragment =
            activity?.supportFragmentManager?.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            this.state = BottomSheetBehavior.STATE_DRAGGING
        }

        // Set click listener for booking appointment
        binding.btnBookAppointment.setOnClickListener {
//            val fragmentManager = childFragmentManager  // Use childFragmentManager
//            val dialogFragment = AppointmentDialogFragment(
//                slotViewModel,
//                args,
//                firebaseAuth.currentUser?.email.toString(),
//                firebaseAuth.currentUser?.phoneNumber.toString()
//            )

            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.appointment_dialog_layout)
            val btnBookVideoConsult = dialog.findViewById<Button>(R.id.btnBookVideoConsult)
            val btnBookClinicVisit = dialog.findViewById<Button>(R.id.btnBookClinicVisit)
            val tvVideoConsultPrice = dialog.findViewById<TextView>(R.id.tvVideoConsultPrice)
            val tvClinicVisitPrice = dialog.findViewById<TextView>(R.id.tvClinicVisitPrice)

            tvVideoConsultPrice.setText("₹" + args?.doctor?.video_consult.toString())
            tvClinicVisitPrice.setText("₹" + args?.doctor?.clinic_visit.toString())

            dialog.show()

            val dialogTiming = Dialog(requireContext())
            dialogTiming.setContentView(R.layout.appointment_timing_dialog)

            val tvPrice = dialogTiming.findViewById<TextView>(R.id.tvPrice)
            val tvConsultText = dialogTiming.findViewById<TextView>(R.id.tvConsultText)
            val btnBook = dialogTiming.findViewById<Button>(R.id.btnBook)

            btnBookVideoConsult.setOnClickListener {
                dialog.dismiss()

                tvPrice.setText("₹" + args?.doctor?.video_consult.toString())
                tvConsultText.setText("Video Consult")
                dialogTiming.show()

                setupAppointmentTimingsRecylerView(dialogTiming.findViewById<RecyclerView>(R.id.appointmentTimeRecylerView))

                btnBook.setOnClickListener {
                    if (slotAdapter.singleSelection == false) {
                        Toast.makeText(
                            requireActivity(),
                            "Please Select An Appointment Time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialogTiming.dismiss()
                        makePayment(
                            args?.doctor?.video_consult,
                            firebaseAuth.currentUser?.email.toString(),
                            firebaseAuth.currentUser?.phoneNumber.toString()
                        )
                    }
                    storeTime = slotAdapter.asynListDiffer.currentList[slotAdapter.lastPosition]
                    storeConsultText = "Video Consult"
                }
            }

            btnBookClinicVisit.setOnClickListener {
                dialog.dismiss()

                tvPrice.setText("₹" + args?.doctor?.clinic_visit.toString())
                tvConsultText.setText("Clinic Visit")
                dialogTiming.show()

                setupAppointmentTimingsRecylerView(dialogTiming.findViewById<RecyclerView>(R.id.appointmentTimeRecylerView))

                btnBook.setOnClickListener {
                    if (slotAdapter.singleSelection == false) {
                        Toast.makeText(
                            requireActivity(),
                            "Please Select An Appointment Time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialogTiming.dismiss()
                        makePayment(
                            args?.doctor?.clinic_visit,
                            firebaseAuth.currentUser?.email.toString(),
                            firebaseAuth.currentUser?.phoneNumber.toString()
                        )
                    }
                    storeTime = slotAdapter.asynListDiffer.currentList[slotAdapter.lastPosition]
                    storeConsultText = "Clinic Visit"
                }
            }



//            dialogFragment.show(fragmentManager, "Appointment Dialog")
        }

        args?.doctor?.let { Log.d("ARGUMENTSNULLLCHECK", it.Name) }

        // Set up header view
        setupHeaderView()
        // Set up bottom sheet content
        setupBottomSheet()

    }

    private fun setupAppointmentTimingsRecylerView(recylerView: RecyclerView) {
        args?.let { slotViewModel.getAllSlots(it) }
        slotAdapter = AppointmentTimeAdapter(requireActivity(), recylerView)
        lifecycleScope.launch(Dispatchers.IO) {
            slotViewModel.allSlotFlow.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(Constants.APPOINTMENTTIMINGSLOT, "Error Block:- ${it?.message.toString()}")
                    }

                    is NetworkResult.Loading -> Log.d(
                        Constants.APPOINTMENTTIMINGSLOT,
                        "Loading Block:- ${it?.message.toString()}"
                    )

                    is NetworkResult.Success -> {
                        Log.d(Constants.APPOINTMENTTIMINGSLOT, "Success Block:- ${it?.data.toString()}")
                        withContext(Dispatchers.Main) {

                            slotAdapter.setData(it.data?.toList()!!)

                            recylerView.apply {
                                adapter = slotAdapter
                                layoutManager =
                                    StaggeredGridLayoutManager(
                                        2,
                                        StaggeredGridLayoutManager.HORIZONTAL
                                    )
                            }
                        }
                    }

                    else -> {
                        Log.d(Constants.APPOINTMENTTIMINGSLOT, "Else Block:- ${it?.message.toString()}")
                    }
                }
            }
        }
    }

    fun makePayment(
        consultPrice: Int?,
        email: String,
        phoneNumber: String
    ){
        if (args != null) {
            val co = Checkout()
            co.setKeyID("rzp_test_wv1GchOQB2x3CE")
            co.setImage(R.mipmap.ic_launcher)
            try {
                val options = JSONObject()
                options.put("name", "Health Care")
                options.put("description", "Consultation Charges")
                //You can omit the image option to fetch the image from the dashboard
                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
                options.put("theme.color", "#6750A4");
                options.put("currency", "INR");
                options.put(
                    "amount",
                    "${consultPrice?.times(100).toString()}"
                )//pass amount in currency subunits

                val retryObj = JSONObject()
                retryObj.put("enabled", true);
                retryObj.put("max_count", 4);
                options.put("retry", retryObj);

                val prefill = JSONObject()

                val email =   email
                val phoneNumber = phoneNumber

                prefill.put("email", email)
                prefill.put("contact", phoneNumber)

                options.put("prefill", prefill)
                co.open(activity, options)

//                val paymentIntent = Intent(requireContext(), PaymentActivity::class.java)
//
//                // Start PaymentActivity with startActivityForResult
//                startPaymentForResult.launch(paymentIntent)

            } catch (e: Exception) {
                Toast.makeText(
                    requireActivity(),
                    "Error in payment: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }

        } else {
            Toast.makeText(
                requireActivity(),
                "Error: Doctor details are null",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onResume() {
        super.onResume()
//        doctorName = args.doctor.Name
//        doctorId = args.doctor.Id
        Log.d("ARGUMENTSNULLLCHECK", "RESUME:- " + (args?.doctor?.Name ?: ""))
    }

    // Method to set up the header view
    private fun setupHeaderView() {
        // Load doctor's profile picture using Glide
        Glide.with(this).load(args?.doctor?.Profile_Pic?.toUri()).into(binding.ivDoctorImage)
        binding.tvDoctorNameInDetailView.text = args?.doctor?.Name ?: ""
        binding.tvSpecializationDetailedView.text = args?.doctor?.Specialization

    }

    // Method to set up the bottom sheet content
    private fun setupBottomSheet() {
        // Set up expandable text view for doctor's about information
        args?.doctor?.let { binding.tvAboutDoctorDetailedView.setResizableText(it.About, 4, true) }

        // Set up RecyclerView for reviews and ratings
        if (!args?.doctor?.Reviews_And_Ratings?.get(0)?.date.equals("")) {
            Log.d("DATATESTING", args?.doctor?.Reviews_And_Ratings?.get(0)?.date.toString())
            setupRatingsRecylerView()
        } else {
            binding.textView4.apply {
                visibility = View.GONE
            }
            binding.ratingCard.tvDoctorRating.visibility = View.GONE
            binding.ratingCard.materialCardView2.visibility = View.GONE

            binding.tvRatingNumber.visibility = View.GONE
        }

        // Set up RecyclerView for services chips
        setupServicesChipView()

        // Set other doctor's information
        binding.tvWorkingHours.text = args?.doctor?.Working_Hours ?: ""
        binding.tvCity.text = args?.doctor?.City ?: ""
        binding.tvAddress.text = args?.doctor?.Address ?: ""
        binding.tvConsultationFee.text = "₹" + args?.doctor?.video_consult.toString()
    }

    // Method to set up the RecyclerView for reviews and ratings
    private fun setupRatingsRecylerView() {
        // Calculate and display average rating
        val averageRatingTextView = binding.ratingCard.tvDoctorRating
        val averageRating =
            args?.doctor?.Reviews_And_Ratings?.averageRating() // Extension function for calculating the average rating of a list of ReviewsAndRatings
        val formattedRating =
            String.format("%.1f", averageRating)
        if (formattedRating == "0.0") {
            averageRatingTextView.text = "No Reviews"
        } else {
            averageRatingTextView.text = formattedRating
        }
        // Display number of ratings
        binding.tvRatingNumber.text = "(${args?.doctor?.Reviews_And_Ratings?.size.toString()})"

        reviewsAndRatingsAdapter = ReviewsAndRatingsAdapter()
        reviewsAndRatingsAdapter.setData(args?.doctor?.Reviews_And_Ratings!!)
        binding.reviewRecylerView.apply {
            adapter = reviewsAndRatingsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    // Method to set up the RecyclerView for services chips
    private fun setupServicesChipView() {
        servicesChipAdatpter = ServicesChipAdatpter()
        args?.doctor?.let { servicesChipAdatpter.setData(it.Services) }
        binding.chipRecylerView.apply {
            adapter = servicesChipAdatpter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }


    fun paymentStatus(paymentStatus: String) {
        Log.d("PAYMENTRESULT", "Doctor View Data:- ${paymentStatus}")

        if (paymentStatus == "Success") {
            try {
                addAppointmentToTheFirebase()
            } catch (e: Exception) {
                Log.d("PAYMENTERROR", "Calling Func:- " + e.message.toString())
            }
        } else {
            Toast.makeText(requireContext(), "Payment Error Occured", Toast.LENGTH_SHORT).show()
        }
    }


    fun addAppointmentToTheFirebase() {
        Log.d("PAYMENTSTATUS", "makepayment")
//
//        Log.d("PARAMETERCHECKING", doctorName)
//        Log.d("PARAMETERCHECKING", storeConsultText)
        firebaseAuth= FirebaseAuth.getInstance()
        args?.doctor?.let { Log.d("PARAMETERCHECKING", it.Name) }
        args?.doctor?.let { Log.d("PARAMETERCHECKING", it.Id) }

        val timestampObject = Timestamp(Date(storeTime))
        val doctorsRef = "/Doctors/${args?.doctor?.Id}"
        val userRef = "/Users/${firebaseAuth.currentUser?.uid.toString()}"

        val userAppointment =
            UserAppointment("Scheduled", storeConsultText, timestampObject, doctorsRef)
        val doctorAppointment =
            DoctorAppointment("Scheduled", storeConsultText, timestampObject, userRef)
        args?.doctor?.let {
            appointmentViewModel.addAppointmentToTheFirebase(
                userAppointment,
                it.Id,
                doctorAppointment
            )
        }



        lifecycleScope.launch(Dispatchers.IO) {

            appointmentViewModel.addAppointment.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                        Log.d(
                            APPOINTMENTADDED,
                            "Error Block:- ${it?.message.toString()}"
                        )
                    }

                    is NetworkResult.Loading -> {
                        Log.d(
                            APPOINTMENTADDED,
                            "Loading Block:- ${it?.message.toString()}"
                        )
                    }

                    is NetworkResult.Success -> {
                        Log.d(
                            APPOINTMENTADDED,
                            "Success Block:- ${it?.message.toString()}"
                        )
                        Toast.makeText(requireContext(), "Appointment Booked", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {}
                }
            }
        }
    }

    // GoogleMap's onMapReady callback
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}