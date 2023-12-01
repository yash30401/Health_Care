package com.healthcare.yash.preeti.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.AppointmentTimeAdapter
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.APPOINTMENTTIMINGSLOT
import com.healthcare.yash.preeti.viewmodels.SlotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppointmentDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs
) :DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_dialog_layout,null)

        val btnBookVideoConsult = dialogView.findViewById<Button>(R.id.btnBookVideoConsult)
        val btnBookClinicVisit = dialogView.findViewById<Button>(R.id.btnBookClinicVisit)

        builder.setView(dialogView)

        btnBookVideoConsult.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(slotViewModel,args)

            if(fragmentManager!=null){
                dialogFragment.show(fragmentManager,"Appointment Timings")
            }
        }

        btnBookClinicVisit.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(slotViewModel,args)

            if(fragmentManager!=null){
                dialogFragment.show(fragmentManager,"Appointment Timings")
            }
        }

        return builder.create()
    }
}

class AppointmentTimingDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs
) :DialogFragment(){

    private lateinit var slotAdapter: AppointmentTimeAdapter
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_timing_dialog,null)

        builder.setView(dialogView)

        setupAppointmentTimingsRecylerView(dialogView.findViewById<RecyclerView>(R.id.appointmentTimeRecylerView))

        return builder.create()
    }

    private fun setupAppointmentTimingsRecylerView(recylerView: RecyclerView) {
        slotViewModel.getAllSlots(args)
        slotAdapter = AppointmentTimeAdapter(requireActivity())
        lifecycleScope.launch(Dispatchers.IO){
            slotViewModel.allSlotFlow.collect{
                when(it){
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
                              StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.HORIZONTAL)
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
}