package com.healthcare.yash.preeti.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.AppointmentTimeAdapter
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.APPOINTMENTTIMINGSLOT
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.healthcare.yash.preeti.viewmodels.SlotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class for displaying appointment dialog
class AppointmentDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs,
    private val startPayment: AppointmentTimingDialogFragment.StartPayment
) : DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_dialog_layout, null)

        // Find views in the dialog layout
        val btnBookVideoConsult = dialogView.findViewById<Button>(R.id.btnBookVideoConsult)
        val btnBookClinicVisit = dialogView.findViewById<Button>(R.id.btnBookClinicVisit)
        val tvVideoConsultPrice = dialogView.findViewById<TextView>(R.id.tvVideoConsultPrice)
        val tvClinicVisitPrice = dialogView.findViewById<TextView>(R.id.tvClinicVisitPrice)

        tvVideoConsultPrice.setText("₹" + args.doctor.video_consult.toString())
        tvClinicVisitPrice.setText("₹" + args.doctor.clinic_visit.toString())

        builder.setView(dialogView)

        btnBookVideoConsult.setOnClickListener {
            dismiss()

            val fragmentManager = parentFragmentManager  // Use childFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(
                slotViewModel,
                args,
                args.doctor.video_consult,
                "Video Consult",
                startPayment
            )

            dialogFragment.show(fragmentManager, "Appointment Timings")
        }

        btnBookClinicVisit.setOnClickListener {
            dismiss()
            val fragmentManager = parentFragmentManager  // Use childFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(
                slotViewModel,
                args,
                args.doctor.clinic_visit,
                "Clinic Visit",
                startPayment
            )

            dialogFragment.show(fragmentManager, "Appointment Timings")
        }

        return builder.create()
    }
}

// Class for displaying appointment timing dialog
class AppointmentTimingDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs,
    private val consultPrice: Int?,
    private val consultText: String,
    private val startPayment: StartPayment
) : DialogFragment() {

    private lateinit var slotAdapter: AppointmentTimeAdapter
    private val appointmentViewModel by viewModels<AppointmentViewModel>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_timing_dialog, null)

        val tvPrice = dialogView.findViewById<TextView>(R.id.tvPrice)
        val tvConsultText = dialogView.findViewById<TextView>(R.id.tvConsultText)

        val btnBook = dialogView.findViewById<Button>(R.id.btnBook)

        tvPrice.setText("₹" + consultPrice.toString())
        tvConsultText.setText(consultText)

        builder.setView(dialogView)

        val fragmentManager = childFragmentManager
        setupAppointmentTimingsRecylerView(dialogView.findViewById<RecyclerView>(R.id.appointmentTimeRecylerView))

        btnBook.setOnClickListener {
            if (slotAdapter.singleSelection == false) {
                Toast.makeText(
                    requireActivity(),
                    "Please Select An Appointment Time",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                dismiss()
                val selectedDoctor = args.doctor ?: return@setOnClickListener
                val action =
                    AppointmentTimingDialogFragmentDirections.actionAppointmentTimingDialogFragmentToDoctorDetailedView(
                       selectedDoctor
                    )
                findNavController().navigate(action)
                startPayment.makePayment(
                    consultPrice,
                    consultText,
                    slotAdapter.asynListDiffer.currentList[slotAdapter.lastPosition]
                )
            }
        }

        return builder.create()
    }

    // Method to set up appointment timings recycler view
    private fun setupAppointmentTimingsRecylerView(recylerView: RecyclerView) {
        slotViewModel.getAllSlots(args)
        slotAdapter = AppointmentTimeAdapter(requireActivity(), recylerView)
        lifecycleScope.launch(Dispatchers.IO) {
            slotViewModel.allSlotFlow.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(APPOINTMENTTIMINGSLOT, "Error Block:- ${it?.message.toString()}")
                    }

                    is NetworkResult.Loading -> Log.d(
                        APPOINTMENTTIMINGSLOT,
                        "Loading Block:- ${it?.message.toString()}"
                    )

                    is NetworkResult.Success -> {
                        Log.d(APPOINTMENTTIMINGSLOT, "Success Block:- ${it?.data.toString()}")
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
                        Log.d(APPOINTMENTTIMINGSLOT, "Else Block:- ${it?.message.toString()}")
                    }
                }
            }
        }
    }

    // Interface to communicate with the calling class for starting payment
    interface StartPayment {
        fun makePayment(
            consultPrice: Int?,
            consultText: String,
            l: Long
        )
    }
}