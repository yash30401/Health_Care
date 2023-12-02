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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.adapters.AppointmentTimeAdapter
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants.APPOINTMENTTIMINGSLOT
import com.healthcare.yash.preeti.viewmodels.SlotViewModel
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AppointmentDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs
) :DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_dialog_layout,null)

        val btnBookVideoConsult = dialogView.findViewById<Button>(R.id.btnBookVideoConsult)
        val btnBookClinicVisit = dialogView.findViewById<Button>(R.id.btnBookClinicVisit)
        val tvVideoConsultPrice = dialogView.findViewById<TextView>(R.id.tvVideoConsultPrice)
        val tvClinicVisitPrice = dialogView.findViewById<TextView>(R.id.tvClinicVisitPrice)

        tvVideoConsultPrice.setText("₹"+args.doctor.video_consult.toString())
        tvClinicVisitPrice.setText("₹"+args.doctor.clinic_visit.toString())

        builder.setView(dialogView)

        btnBookVideoConsult.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(slotViewModel,args,args.doctor.video_consult,"Video Consult")

            if(fragmentManager!=null){
                dialogFragment.show(fragmentManager,"Appointment Timings")
            }
        }

        btnBookClinicVisit.setOnClickListener {
            val fragmentManager = activity?.supportFragmentManager
            val dialogFragment = AppointmentTimingDialogFragment(
                slotViewModel,
                args,
                args.doctor.clinic_visit,
                "Clinic Visit"
            )

            if(fragmentManager!=null){
                dialogFragment.show(fragmentManager,"Appointment Timings")
            }
        }

        return builder.create()
    }
}

class AppointmentTimingDialogFragment(
    private val slotViewModel: SlotViewModel,
    private val args: DoctorDetailedViewArgs,
    private val consultPrice: Int?,
    private val consultText: String
) :DialogFragment(), PaymentResultWithDataListener, ExternalWalletListener{

    private lateinit var slotAdapter: AppointmentTimeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(requireActivity().applicationContext)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.appointment_timing_dialog,null)

        val tvPrice = dialogView.findViewById<TextView>(R.id.tvPrice)
        val tvConsultText = dialogView.findViewById<TextView>(R.id.tvConsultText)

        val btnBook = dialogView.findViewById<Button>(R.id.btnBook)

        tvPrice.setText("₹"+consultPrice.toString())
        tvConsultText.setText(consultText)

        builder.setView(dialogView)

        setupAppointmentTimingsRecylerView(dialogView.findViewById<RecyclerView>(R.id.appointmentTimeRecylerView))

        btnBook.setOnClickListener {
            startPayment()
        }

        return builder.create()
    }

    private fun setupAppointmentTimingsRecylerView(recylerView: RecyclerView) {
        slotViewModel.getAllSlots(args)
        slotAdapter = AppointmentTimeAdapter(requireActivity(),recylerView)
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

    private fun startPayment() {
        val co = Checkout()
        co.setKeyID("rzp_test_wv1GchOQB2x3CE")


        try {
            val options = JSONObject()
            options.put("name","Razorpay Corp")
            options.put("description","Demoing Charges")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#3399cc");
            options.put("currency","INR");
            options.put("order_id", "order_DBJOWzybf0sJbb");
            options.put("amount","50000")//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email","gaurav.kumar@example.com")
            prefill.put("contact","9876543210")

            options.put("prefill",prefill)
            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {

    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {

    }
}