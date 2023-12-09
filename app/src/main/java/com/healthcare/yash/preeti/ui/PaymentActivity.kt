package com.healthcare.yash.preeti.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.healthcare.yash.preeti.R
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class PaymentActivity : AppCompatActivity(), PaymentResultWithDataListener,ExternalWalletListener{

    private var resultIntent:Intent?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        Log.d("PAYMENTRESULT","Payment Activity Openend")
        resultIntent = Intent()
    }

    override fun onPaymentSuccess(s: String?, paymentData: PaymentData?) {
        Log.d("PAYMENTRESULT","Success")
        resultIntent?.putExtra("PAYMENTSTATUSKEY","Success")
        setResult(Activity.RESULT_OK,resultIntent)
        finish()
    }

    override fun onPaymentError(errorCode: Int, s: String?, paymentData: PaymentData?) {
        resultIntent?.putExtra("PAYMENTSTATUSKEY","Error")
        setResult(Activity.RESULT_OK,resultIntent)
        finish()
    }

    override fun onExternalWalletSelected(s: String?, paymentData: PaymentData?) {
        resultIntent?.putExtra("PAYMENTSTATUSKEY","ExternalWallet")
        setResult(Activity.RESULT_OK,resultIntent)
        finish()
    }
}