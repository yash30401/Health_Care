package com.healthcare.yash.preeti.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.healthcare.yash.preeti.R
import com.healthcare.yash.preeti.models.Doctor
import com.healthcare.yash.preeti.models.DoctorAppointment
import com.healthcare.yash.preeti.models.UserAppointment
import com.healthcare.yash.preeti.networking.NetworkResult
import com.healthcare.yash.preeti.other.Constants
import com.healthcare.yash.preeti.viewmodels.AppointmentViewModel
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity(), PaymentResultWithDataListener, ExternalWalletListener {

    private val appointmentViewModel by viewModels<AppointmentViewModel>()

    lateinit var doctor: Doctor
    var slotTime: Long? = 0L
    lateinit var consultText: String
    lateinit var firebaseCurrentUserId: String
    lateinit var email: String
    lateinit var phoneNumber: String

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        doctor = intent.getSerializableExtra("DOCTOR") as Doctor
        slotTime = intent.getLongExtra("SLOTTIME", 0L)
        consultText = intent.getStringExtra("CONSULTTEXT").toString()
        firebaseCurrentUserId = intent.getStringExtra("FIREBASEUSER").toString()
        email = intent.getStringExtra("EMAIL").toString()
        phoneNumber = intent.getStringExtra("PHONENUMBER").toString()

        if (consultText == "Clinic Visit") {
            makePayment(doctor.clinic_visit, email, phoneNumber)
        } else {
            makePayment(doctor.video_consult, email, phoneNumber)
        }


    }

    fun makePayment(
        consultPrice: Int?,
        email: String,
        phoneNumber: String,
    ) {
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

            val email = email
            val phoneNumber = phoneNumber

            prefill.put("email", email)
            prefill.put("contact", phoneNumber)

            options.put("prefill", prefill)
            co.open(this@PaymentActivity, options)



        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error in payment: " + e.message,
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }

    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Log.d("PAYMENTRESULT", "SUCCESS:- ${p0}")
        try {
            Log.d("PAYMENTRESULT", doctor.Name)
            Log.d("PAYMENTRESULT", slotTime.toString())
            Log.d("PAYMENTRESULT", consultText)
            addAppointmentToTheFirebase()
        } catch (e: Exception) {
            Log.d("PAYMENTERROR", "SUCCESS BLOCK:- " + e.message.toString())
        }

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.d("PAYMENTRESULT", "ERROR:- ${p0}")
        Toast.makeText(this, "Error In Payment", Toast.LENGTH_LONG).show()
    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
    }

    fun addAppointmentToTheFirebase() {
        Log.d("LIFECYCLERFRAG", "Now in addApp")
        Log.d("PAYMENTSTATUS", "makepayment")
//
//        Log.d("PARAMETERCHECKING", doctorName)
//        Log.d("PARAMETERCHECKING", storeConsultText)


        val timestampObject = Timestamp(Date(slotTime!!))
        val doctorsRef = firestore.document("/Doctors/${doctor.Id}")
        val userRef = firestore.document("/Users/${firebaseCurrentUserId}")

        Log.d("PARAMETERCHECKING", doctor!!.Name)
        Log.d("PARAMETERCHECKING", doctor!!.Id)
        Log.d("PARAMETERCHECKING", consultText)
        Log.d("PARAMETERCHECKING", timestampObject.toString())
        Log.d("PARAMETERCHECKING", firebaseCurrentUserId)

        val userAppointment =
            UserAppointment("Scheduled", consultText, timestampObject, doctorsRef)
        val doctorAppointment =
            DoctorAppointment("Scheduled", consultText, timestampObject, userRef)

        appointmentViewModel.addAppointmentToTheFirebase(
            userAppointment,
            doctor.Id,
            doctorAppointment
        )




        CoroutineScope(Dispatchers.IO).launch {

            appointmentViewModel.addAppointment.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PaymentActivity,
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            onBackPressed()
                        }
                        Log.d(
                            Constants.APPOINTMENTADDED,
                            "Error Block:- ${it?.message.toString()}"
                        )
                    }

                    is NetworkResult.Loading -> {
                        Log.d(
                            Constants.APPOINTMENTADDED,
                            "Loading Block:- ${it?.message.toString()}"
                        )
                    }

                    is NetworkResult.Success -> {
                        Log.d(
                            Constants.APPOINTMENTADDED,
                            "Success Block:- ${it?.message.toString()}"
                        )

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PaymentActivity,
                                "Appointment Booked",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            onBackPressed()
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}