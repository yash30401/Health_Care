package com.healthcare.yash.preeti.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.healthcare.yash.preeti.R

class AppointmentDialogFragment:DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_dialog_layout,null)

        val btnBookVideoConsult = dialogView.findViewById<Button>(R.id.btnBookVideoConsult)
        val btnBookClinicVisit = dialogView.findViewById<Button>(R.id.btnBookClinicVisit)

        builder.setView(dialogView)

        btnBookVideoConsult.setOnClickListener {
            Toast.makeText(requireContext(), "Video Consult", Toast.LENGTH_SHORT).show()
        }

        btnBookClinicVisit.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment()

            if(fragmentManager!=null){
                dialogFragment.show(fragmentManager,"Appointment Timings")
            }
        }

        return builder.create()
    }
}

class AppointmentTimingDialogFragment():DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_timing_dialog,null)

        builder.setView(dialogView)

        return builder.create()
    }
}